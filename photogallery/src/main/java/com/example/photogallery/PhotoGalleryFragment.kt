package com.example.photogallery

import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.photogallery.databinding.FragmentPhotoGelleryBinding
import java.util.concurrent.TimeUnit

private const val TAG = "PhotoGalleryFragment"
private const val POLL_WORK = "POLL_WORK"

class PhotoGalleryFragment : VisibleFragment() {

    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

    private lateinit var binding: FragmentPhotoGelleryBinding

    private var photoAdapter = PhotoAdapter()

    private val photoGalleryViewModel: PhotoGalleryViewModel by viewModels()

    val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->

        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_photo_gellery,
            container,
            false
        )

        binding.photoRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = photoAdapter
            viewTreeObserver.addOnGlobalLayoutListener {
                val grid = layoutManager as GridLayoutManager

                if (grid.width < 1500 && grid.spanCount != 3) {
                    grid.spanCount = 3
                } else if (grid.width > 1500 && grid.spanCount != 5) {
                    grid.spanCount = 5
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

        photoGalleryViewModel.galleryItemLiveData.observe(viewLifecycleOwner) { galleryItems ->
            photoAdapter.submitData(lifecycle, galleryItems)
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_photo_gallery, menu)
                val searchItem = menu.findItem(R.id.menu_item_search)
                val searchView = searchItem.actionView as SearchView
                searchView.apply {
                    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(p0: String): Boolean {
                            Log.d(TAG, "QueryTextSubmit: $p0")
                            onActionViewCollapsed()
                            photoGalleryViewModel.fetchPhotos(p0)
                            return true
                        }

                        override fun onQueryTextChange(p0: String): Boolean {
                            Log.d(TAG, "QueryTextChange: $p0")
                            return false
                        }
                    })
                    setOnSearchClickListener {
                        searchView.setQuery(photoGalleryViewModel.searchTerm, false)
                    }
                }
                val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
                val isPolling = QueryPreferences.isPolling(requireContext())
                val toggleItemTitle = if (isPolling) {
                    R.string.stop_polling
                } else {
                    R.string.start_polling
                }
                toggleItem.setTitle(toggleItemTitle)


            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_item_clear -> {
                        photoGalleryViewModel.fetchPhotos("")
                        true
                    }

                    R.id.menu_item_toggle_polling -> {
                        val isPolling = QueryPreferences.isPolling(requireContext())
                        if (isPolling) {
                            WorkManager.getInstance(requireContext()).cancelUniqueWork(POLL_WORK)
                            QueryPreferences.setPolling(requireContext(), false)
                        } else {
                            val constraints = Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.UNMETERED)
                                .build()

                            val periodRequest = PeriodicWorkRequest
                                .Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
                                .setConstraints(constraints)
                                .build()
                            WorkManager.getInstance(requireContext())
                                .enqueueUniquePeriodicWork(
                                    POLL_WORK,
                                    ExistingPeriodicWorkPolicy.KEEP,
                                    periodRequest
                                )
                            QueryPreferences.setPolling(requireContext(), true)
                        }
                        activity?.invalidateOptionsMenu()
                        true
                    }

                    else -> false
                }
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        val responseHandler = Handler(Looper.getMainLooper())
        val am = requireContext().getSystemService<ActivityManager>()
        val memory = am?.memoryClass ?: 1
        thumbnailDownloader =
            ThumbnailDownloader(memory * 1024 / 8, responseHandler) { photoHolder, bitmap ->
                val drawable = BitmapDrawable(resources, bitmap)
                photoHolder.bindDrawable(drawable)
            }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)

        if (
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED &&
            !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onStart() {
        super.onStart()
        photoAdapter.addLoadStateListener { loadState ->
            binding.loadState = loadState
            val errorState = when {
                loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                else -> null
            }
            errorState?.let {
                Toast.makeText(context, it.error.toString(), Toast.LENGTH_LONG).show()
            }


        }
        binding.buttonRefresh.setOnClickListener {
            photoAdapter.refresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        thumbnailDownloader.clearing()


    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Destroying background thread")
        lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    private inner class PhotoHolder(private val itemImageView: ImageView) :
        RecyclerView.ViewHolder(itemImageView), View.OnClickListener {
        private lateinit var galleryItem: GalleryItem

        val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable

        fun bindGalleryItem(item: GalleryItem) {
            galleryItem = item
        }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val intent = PhotoPageActivity.newIntent(requireContext(),galleryItem.photoPageUri)
            startActivity(intent)
//            val params =  CustomTabColorSchemeParams.Builder()
//                .setToolbarColor(ContextCompat.getColor(requireContext(),
//                    com.google.android.material.R.color.design_default_color_primary))
//                .build()
//            CustomTabsIntent.Builder()
//                .setDefaultColorSchemeParams(params)
//                .setShowTitle(true)
//                .build()
//                .launchUrl(requireContext(), galleryItem.photoPageUri)
        }
    }

    private inner class PhotoAdapter :
        PagingDataAdapter<GalleryItem, PhotoHolder>(object : DiffUtil.ItemCallback<GalleryItem>() {
            override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
                return oldItem == newItem
            }

        }) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val view = layoutInflater.inflate(
                R.layout.list_item_gallery,
                parent,
                false
            ) as ImageView
            return PhotoHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = getItem(position)!!
            holder.bindGalleryItem(galleryItem)
            val placeholder = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.bill_up_close
            ) ?: ColorDrawable()
            holder.bindDrawable(placeholder)
            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
        }

    }
}
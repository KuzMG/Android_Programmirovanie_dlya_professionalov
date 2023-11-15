package com.example.beatbox

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

class SoundViewModelTest {
    @Mock
    private lateinit var  beatBox: BeatBox
    private lateinit var sound: Sound
    private lateinit var subject: SoundViewModel

    @Before
    fun setUp() {
        beatBox = mock(BeatBox::class.java)
        sound = Sound("assetPath")
        subject = SoundViewModel(beatBox)
        subject.sound = sound
    }
    @Test
    fun exposesSoundNameAsTitle() {
        assertThat(subject.title,`is`(sound.name))
    }
    @Test
    fun callBeatBoxPlayOnButtonClicked(){
        subject.onButtonCkicked()

        verify(beatBox).play(sound)
    }
}
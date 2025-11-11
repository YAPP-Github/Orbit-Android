package com.yapp.domain.media

import com.yapp.domain.model.AlarmSound

interface AlarmSoundManager {
    fun getAlarmSounds(): List<AlarmSound>
    fun initializeSoundPlayer(uri: String)
    fun playAlarmSound(volume: Int)
    fun stopAlarmSound()
    fun updateAlarmVolume(volume: Int)
    fun releaseSoundPlayer()
}

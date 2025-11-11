package com.yapp.media.sound

import android.net.Uri
import com.yapp.domain.media.AlarmSoundManager
import com.yapp.domain.model.AlarmSound
import com.yapp.media.ringtone.RingtoneManagerHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmSoundManagerImpl @Inject constructor(
    private val ringtoneManagerHelper: RingtoneManagerHelper,
    private val soundPlayer: SoundPlayer,
) : AlarmSoundManager {

    override fun getAlarmSounds(): List<AlarmSound> =
        ringtoneManagerHelper.getAlarmSounds().map { (title, uri) ->
            AlarmSound(title = title, uri = uri.toString())
        }

    override fun initializeSoundPlayer(uri: String) {
        if (uri.isBlank()) return
        soundPlayer.initialize(Uri.parse(uri))
    }

    override fun playAlarmSound(volume: Int) {
        soundPlayer.playSound(volume)
    }

    override fun stopAlarmSound() {
        soundPlayer.stopSound()
    }

    override fun updateAlarmVolume(volume: Int) {
        soundPlayer.updateVolume(volume)
    }

    override fun releaseSoundPlayer() {
        soundPlayer.release()
    }
}

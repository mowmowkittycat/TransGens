package mc.warp.transmitgenerators.type

import com.google.gson.annotations.SerializedName


class WarpSound {
    @SerializedName("sound") var soundName: String;
    @SerializedName("pitch") var pitch: Float;
    @SerializedName("volume") var volume: Float;

    constructor(name: String, pitch: Float, volume: Float) {
        this.soundName = name
        this.pitch = pitch
        this.volume = volume
    }

}
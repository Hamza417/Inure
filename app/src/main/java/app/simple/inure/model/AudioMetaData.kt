package app.simple.inure.model

class AudioMetaData(
        /**
         * Name of the song
         */
        var title: String? = null,

        /**
         * Artists of the song
         */
        var artists: String? = null,

        /**
         * Album of the song
         */
        var album: String? = null,

        /**
         * Bitrate of the song
         */
        var bitrate: String? = null,

        /**
         * File extension
         */
        var format: String? = null,

        /**
         * frequency of the audio file in KHz
         */
        var sampling: String? = null,
)
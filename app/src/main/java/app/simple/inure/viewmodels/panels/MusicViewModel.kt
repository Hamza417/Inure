package app.simple.inure.viewmodels.panels

import android.annotation.SuppressLint
import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.AudioModel
import app.simple.inure.preferences.MusicPreferences
import app.simple.inure.util.SortMusic.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : WrappedViewModel(application) {

    private var cursor: Cursor? = null
    private var globalList = arrayListOf<AudioModel>()

    init {
        loadData()
    }

    private val songs: MutableLiveData<ArrayList<AudioModel>> by lazy {
        MutableLiveData<ArrayList<AudioModel>>()
    }

    private val searched: MutableLiveData<ArrayList<AudioModel>> by lazy {
        MutableLiveData<ArrayList<AudioModel>>()
    }

    fun getSongs(): LiveData<ArrayList<AudioModel>> {
        return songs
    }

    fun getSearched(): LiveData<ArrayList<AudioModel>> {
        return searched
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.Default) {
            globalList = getAllAudioFiles(externalContentUri).getSortedList()
            songs.postValue(globalList)
            loadSearched(MusicPreferences.getSearchKeyword())
        }
    }

    /**
     * Returns an Arraylist of [AudioModel]
     */
    @SuppressLint("Range", "InlinedApi")
    private fun getAllAudioFiles(contentLocation: Uri): ArrayList<AudioModel> {
        val allAudioModel = ArrayList<AudioModel>()

        cursor = context.contentResolver.query(
                contentLocation,
                audioProjection,
                selection,
                null,
                "LOWER (" + MediaStore.Audio.Media.TITLE + ") ASC")

        if (cursor != null && cursor!!.moveToFirst()) {
            do {
                val audioModel = AudioModel()
                val albumId = cursor!!.getLong(cursor!!.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))

                audioModel.name = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                audioModel.title = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Audio.Media.TITLE))
                audioModel.id = cursor!!.getLong(cursor!!.getColumnIndex(MediaStore.Audio.Media._ID))
                audioModel.fileUri = Uri.withAppendedPath(contentLocation, audioModel.id.toString()).toString()
                audioModel.path = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Audio.Media.DATA))
                audioModel.size = cursor!!.getInt(cursor!!.getColumnIndex(MediaStore.Audio.Media.SIZE))
                audioModel.album = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                audioModel.artists = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                audioModel.duration = cursor!!.getLong(cursor!!.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                audioModel.dateAdded = cursor!!.getLong(cursor!!.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED))
                audioModel.dateModified = cursor!!.getLong(cursor!!.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))
                audioModel.dateTaken = cursor!!.getLong(cursor!!.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_TAKEN))
                audioModel.artUri = Uri.withAppendedPath(Uri.parse("content://media/external/audio/albumart"), albumId.toString()).toString()
                audioModel.track = cursor!!.getInt(cursor!!.getColumnIndex(MediaStore.Audio.Media.TRACK))
                audioModel.mimeType = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE))
                audioModel.year = cursor!!.getInt(cursor!!.getColumnIndex(MediaStore.Audio.Media.YEAR))

                //for android 10 exclusively
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Uri contentUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                    try {
                        AssetFileDescriptor file = audioContext.getContentResolver().openAssetFileDescriptor(contentUri, "r");
                        audioContent.setMusicPathQ(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }*/

                allAudioModel.add(audioModel)
            } while (cursor!!.moveToNext())
            cursor!!.close()
        }

        return allAudioModel
    }

    fun loadSearched(keywords: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val list = arrayListOf<AudioModel>()

            kotlin.runCatching {
                if (keywords.isNotEmpty()) {
                    for (song in globalList) {
                        if (song.name.lowercase().contains(keywords.lowercase())
                            || song.artists.lowercase().contains(keywords.lowercase())
                            || song.album.lowercase().contains(keywords.lowercase())) {
                            list.add(song)
                        }
                    }
                }
            }

            searched.postValue(list)
        }
    }

    fun shuffleSongs() {
        viewModelScope.launch(Dispatchers.Default) {
            globalList.shuffle()
            songs.postValue(globalList)
        }
    }

    fun sortSongs() {
        viewModelScope.launch(Dispatchers.Default) {
            globalList = globalList.getSortedList()
            songs.postValue(globalList)
        }
    }

    @Suppress("unused")
    companion object {
        @SuppressLint("InlinedApi")
        val audioProjection = arrayOf(
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.DATE_TAKEN,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.YEAR
        )

        const val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val externalContentUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val internalContentUri: Uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI
    }
}
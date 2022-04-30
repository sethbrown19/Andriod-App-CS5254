package edu.vt.cs.cs5254.gallery

import androidx.lifecycle.ViewModel
import edu.vt.cs.cs5254.gallery.api.FlickrFetchr

class PhotoGalleryViewModel : ViewModel() {
    val galleryItemLiveData = FlickrFetchr.fetchPhotos()
}
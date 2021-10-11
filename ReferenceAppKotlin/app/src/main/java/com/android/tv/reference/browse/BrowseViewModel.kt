/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tv.reference.browse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.android.tv.reference.TvReferenceApplication
import com.android.tv.reference.auth.UserManager
import com.android.tv.reference.repository.VideoRepository
import com.android.tv.reference.repository.VideoRepositoryFactory
import com.android.tv.reference.shared.datamodel.VideoGroup
import com.defsub.takeout.tv.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class BrowseViewModel(application: Application) : AndroidViewModel(application) {
    private val videoRepository = VideoRepositoryFactory.getVideoRepository()
    private val userManager = UserManager.getInstance(application.applicationContext)
    val browseContent = MutableLiveData<List<VideoGroup>>()
    val customMenuItems = MutableLiveData<List<BrowseCustomMenu>>(listOf())
    val isSignedIn = Transformations.map(userManager.userInfo) { it != null }

    init {
        viewModelScope.launch {
            Timber.d("refresh ${userManager.userInfo.value}")
            Timber.d("refresh $videoRepository")
            asyncRefresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            asyncRefresh()
        }
    }

    private suspend fun asyncRefresh() {
        val list = getVideoGroupList(videoRepository)
        browseContent.value = list
    }

    suspend fun getVideoGroupList(repository: VideoRepository): List<VideoGroup> {
        val videosByCategory = repository.getAllVideos().groupBy { it.category }
        val videoGroupList = mutableListOf<VideoGroup>()
        val context = getApplication<TvReferenceApplication>()
        videoGroupList.add(VideoGroup(
            context.getString(R.string.new_releases),
            repository.getNewReleases()))
        videoGroupList.add(VideoGroup(context.getString(R.string.recently_added),
            repository.getRecentlyAdded()))
        videosByCategory.forEach { (k, v) ->
            videoGroupList.add(VideoGroup(k, v))
        }
        return videoGroupList
    }

    fun signOut() = viewModelScope.launch(Dispatchers.IO) {
        userManager.signOut()
    }
}

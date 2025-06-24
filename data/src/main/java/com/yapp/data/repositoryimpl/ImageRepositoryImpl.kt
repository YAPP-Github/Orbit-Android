package com.yapp.data.repositoryimpl

import com.yapp.data.local.datasource.ImageLocalDataSource
import com.yapp.domain.repository.ImageRepository
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val imageLocalDataSource: ImageLocalDataSource,
) : ImageRepository {

    override suspend fun saveImage(byteArray: ByteArray): Boolean {
        return imageLocalDataSource.saveImage(byteArray)
    }
}

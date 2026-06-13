package com.example.propaintersplastererspayment.data.local.dao

import androidx.room.*
import com.example.propaintersplastererspayment.core.util.AddressKeyUtils
import com.example.propaintersplastererspayment.data.local.entity.AccessItemEntity
import com.example.propaintersplastererspayment.data.local.entity.PropertyAccessItemEntity
import com.example.propaintersplastererspayment.data.local.entity.PropertyAccessProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccessDao {
    @Query("SELECT * FROM access_items WHERE jobId = :jobId")
    fun observeAccessItems(jobId: Long): Flow<List<AccessItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccessItem(item: AccessItemEntity): Long

    @Update
    suspend fun updateAccessItem(item: AccessItemEntity)

    @Delete
    suspend fun deleteAccessItem(item: AccessItemEntity)

    @Query("SELECT propertyAddress FROM jobs WHERE jobId = :jobId LIMIT 1")
    suspend fun getJobAddress(jobId: Long): String?

    @Query("SELECT * FROM access_items WHERE jobId = :jobId ORDER BY accessId")
    suspend fun getAccessItems(jobId: Long): List<AccessItemEntity>

    @Query("SELECT * FROM property_access_profiles WHERE addressKey = :addressKey LIMIT 1")
    suspend fun getPropertyAccessProfile(addressKey: String): PropertyAccessProfileEntity?

    @Insert
    suspend fun insertPropertyAccessProfile(profile: PropertyAccessProfileEntity): Long

    @Update
    suspend fun updatePropertyAccessProfile(profile: PropertyAccessProfileEntity)

    @Query("DELETE FROM property_access_items WHERE profileId = :profileId")
    suspend fun deletePropertyAccessItems(profileId: Long)

    @Insert
    suspend fun insertPropertyAccessItems(items: List<PropertyAccessItemEntity>)

    @Transaction
    suspend fun saveAccessItemAndSyncProperty(item: AccessItemEntity): Long {
        val accessId = if (item.accessId == 0L) {
            insertAccessItem(item)
        } else {
            updateAccessItem(item)
            item.accessId
        }
        syncPropertyProfileFromJob(item.jobId)
        return accessId
    }

    @Transaction
    suspend fun deleteAccessItemAndSyncProperty(item: AccessItemEntity) {
        deleteAccessItem(item)
        syncPropertyProfileFromJob(item.jobId)
    }

    suspend fun syncPropertyProfileFromJob(jobId: Long) {
        val displayAddress = getJobAddress(jobId)?.trim().orEmpty()
        val addressKey = AddressKeyUtils.normalize(displayAddress)
        if (addressKey.isBlank()) return

        val now = System.currentTimeMillis()
        val existingProfile = getPropertyAccessProfile(addressKey)
        val profileId = if (existingProfile == null) {
            insertPropertyAccessProfile(
                PropertyAccessProfileEntity(
                    addressKey = addressKey,
                    displayAddress = displayAddress,
                    updatedAt = now
                )
            )
        } else {
            updatePropertyAccessProfile(
                existingProfile.copy(
                    displayAddress = displayAddress,
                    updatedAt = now
                )
            )
            existingProfile.profileId
        }

        deletePropertyAccessItems(profileId)
        val currentItems = getAccessItems(jobId)
        if (currentItems.isNotEmpty()) {
            insertPropertyAccessItems(
                currentItems.map { item ->
                    PropertyAccessItemEntity(
                        profileId = profileId,
                        type = item.type,
                        code = item.code,
                        instructions = item.instructions
                    )
                }
            )
        }
    }
}

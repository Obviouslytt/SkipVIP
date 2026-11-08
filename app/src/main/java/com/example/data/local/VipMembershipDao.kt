package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VipMembership
import kotlinx.coroutines.flow.Flow

@Dao
interface VipMembershipDao {
    @Query("SELECT * FROM vip_memberships ORDER BY isActive DESC, name ASC")
    fun getAllMembershipsFlow(): Flow<List<VipMembership>>

    @Query("SELECT * FROM vip_memberships WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getActiveMembershipsSync(): List<VipMembership>

    @Query("SELECT * FROM vip_memberships WHERE id = :id LIMIT 1")
    suspend fun getMembershipById(id: Long): VipMembership?

    @Query("SELECT COUNT(*) FROM vip_memberships")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(membership: VipMembership): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memberships: List<VipMembership>)

    @Update
    suspend fun update(membership: VipMembership)

    @Delete
    suspend fun delete(membership: VipMembership)

    @Query("DELETE FROM vip_memberships WHERE id = :id")
    suspend fun deleteById(id: Long)
}

package com.example.data.repository

import com.example.data.local.SkipRecordDao
import com.example.data.local.VipMembershipDao
import com.example.data.model.SkipRecord
import com.example.data.model.VipMembership
import kotlinx.coroutines.flow.Flow

class VipRepository(
    private val membershipDao: VipMembershipDao,
    private val skipRecordDao: SkipRecordDao
) {
    val allMembershipsFlow: Flow<List<VipMembership>> = membershipDao.getAllMembershipsFlow()

    fun getRecordsForMonthFlow(year: Int, month: Int): Flow<List<SkipRecord>> {
        return skipRecordDao.getRecordsForMonthFlow(year, month)
    }

    val allTimeSavedFlow: Flow<Double> = skipRecordDao.getAllTimeSavedFlow()
    val allTimeSkipsCountFlow: Flow<Int> = skipRecordDao.getAllTimeSkipsCountFlow()
    val allRecordsFlow: Flow<List<SkipRecord>> = skipRecordDao.getAllRecordsFlow()

    suspend fun markSkipped(membership: VipMembership, year: Int, month: Int) {
        skipRecordDao.insertOrUpdate(
            SkipRecord(
                membershipId = membership.id,
                year = year,
                month = month,
                status = "SKIPPED",
                actionTimestamp = System.currentTimeMillis(),
                savedAmount = membership.monthlyCost,
                note = "Skipped on $month/$year"
            )
        )
    }

    suspend fun markBilled(membership: VipMembership, year: Int, month: Int) {
        skipRecordDao.insertOrUpdate(
            SkipRecord(
                membershipId = membership.id,
                year = year,
                month = month,
                status = "BILLED",
                actionTimestamp = System.currentTimeMillis(),
                savedAmount = 0.0,
                note = "Charged $month/$year"
            )
        )
    }

    suspend fun unmarkStatus(membershipId: Long, year: Int, month: Int) {
        skipRecordDao.deleteRecord(membershipId, year, month)
    }

    suspend fun addMembership(membership: VipMembership): Long {
        return membershipDao.insert(membership)
    }

    suspend fun updateMembership(membership: VipMembership) {
        membershipDao.update(membership)
    }

    suspend fun deleteMembership(membership: VipMembership) {
        skipRecordDao.deleteRecordsForMembership(membership.id)
        membershipDao.delete(membership)
    }
}

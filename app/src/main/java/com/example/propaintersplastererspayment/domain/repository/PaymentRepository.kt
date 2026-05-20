package com.example.propaintersplastererspayment.domain.repository

import com.example.propaintersplastererspayment.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun getAllPaymentsStream(): Flow<List<PaymentEntity>>
    fun getPaymentsForClientStream(clientId: Long): Flow<List<PaymentEntity>>
    suspend fun getPayment(id: Long): PaymentEntity?
    suspend fun insertPayment(payment: PaymentEntity): Long
    suspend fun deletePayment(payment: PaymentEntity)
    suspend fun updatePayment(payment: PaymentEntity)
}

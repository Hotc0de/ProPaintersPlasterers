package com.example.propaintersplastererspayment.data.repository

import com.example.propaintersplastererspayment.data.local.dao.PaymentDao
import com.example.propaintersplastererspayment.data.local.entity.PaymentEntity
import com.example.propaintersplastererspayment.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow

class OfflinePaymentRepository(private val paymentDao: PaymentDao) : PaymentRepository {
    override fun getAllPaymentsStream(): Flow<List<PaymentEntity>> = paymentDao.getAllPayments()

    override fun getPaymentsForClientStream(clientId: Long): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsForClient(clientId)

    override suspend fun getPayment(id: Long): PaymentEntity? = paymentDao.getPaymentById(id)

    override suspend fun insertPayment(payment: PaymentEntity): Long = paymentDao.insert(payment)

    override suspend fun deletePayment(payment: PaymentEntity) = paymentDao.delete(payment)

    override suspend fun updatePayment(payment: PaymentEntity) = paymentDao.update(payment)

    override suspend fun deletePaymentsForClient(clientId: Long) =
        paymentDao.deletePaymentsForClient(clientId)
}

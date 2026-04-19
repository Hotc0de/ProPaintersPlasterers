package com.example.propaintersplastererspayment.app.navigation

object AppDestinations {
    // Core
    const val SPLASH_ROUTE          = "splash"
    const val SELECTION_ROUTE       = "selection"
    const val HOME_ROUTE            = "home"
    const val PAINT_ROUTE           = "paint"
    const val SETTINGS_ROUTE        = "settings"
    const val INITIAL_SETUP_ROUTE   = "initial_setup"

    // Jobs
    const val JOB_FORM_ROUTE       = "job_form"
    const val JOB_DETAIL_ROUTE     = "job_detail"
    const val JOB_ID_ARG           = "jobId"
    const val JOB_FORM_WITH_ARG    = "$JOB_FORM_ROUTE?$JOB_ID_ARG={$JOB_ID_ARG}"
    const val JOB_DETAIL_WITH_ARG  = "$JOB_DETAIL_ROUTE/{$JOB_ID_ARG}"

    fun jobFormRoute(jobId: Long?): String =
        if (jobId == null) JOB_FORM_ROUTE else "$JOB_FORM_ROUTE?$JOB_ID_ARG=$jobId"

    fun jobDetailRoute(jobId: Long): String = "$JOB_DETAIL_ROUTE/$jobId"

    // Clients
    const val CLIENTS_ROUTE       = "clients"
    const val CLIENT_FORM_ROUTE   = "client_form"
    const val CLIENT_ID_ARG       = "clientId"
    const val CLIENT_FORM_WITH_ARG = "$CLIENT_FORM_ROUTE?$CLIENT_ID_ARG={$CLIENT_ID_ARG}"

    fun clientFormRoute(clientId: Long?): String =
        if (clientId == null) CLIENT_FORM_ROUTE else "$CLIENT_FORM_ROUTE?$CLIENT_ID_ARG=$clientId"

    // Invoices
    const val INVOICE_ROUTE        = "invoice"
    const val INVOICE_CREATE_ROUTE = "invoice_create"
    const val IS_QUICK_INVOICE_ARG = "isQuickInvoice"
    const val INVOICE_WITH_ARG     = "$INVOICE_ROUTE/{$JOB_ID_ARG}?$IS_QUICK_INVOICE_ARG={$IS_QUICK_INVOICE_ARG}"

    fun invoiceRoute(jobId: Long, isQuickInvoice: Boolean = false): String = 
        "$INVOICE_ROUTE/$jobId?$IS_QUICK_INVOICE_ARG=$isQuickInvoice"
    fun invoiceCreateRoute(): String = INVOICE_CREATE_ROUTE

    // Paint
    const val PAINT_BRAND_DETAIL_ROUTE = "paint_brand_detail"
    const val PAINT_ITEM_FORM_ROUTE    = "paint_item_form"
    const val BRAND_ID_ARG             = "brandId"
    const val PAINT_ID_ARG             = "paintId"
    const val PAINT_BRAND_DETAIL_WITH_ARG = "$PAINT_BRAND_DETAIL_ROUTE/{$BRAND_ID_ARG}"
    const val PAINT_ITEM_FORM_WITH_ARG    = "$PAINT_ITEM_FORM_ROUTE?$BRAND_ID_ARG={$BRAND_ID_ARG}&$PAINT_ID_ARG={$PAINT_ID_ARG}"

    fun paintBrandDetailRoute(brandId: Long): String = "$PAINT_BRAND_DETAIL_ROUTE/$brandId"
    fun paintItemFormRoute(brandId: Long?, paintId: Long?): String {
        val bArg = if (brandId != null) "$BRAND_ID_ARG=$brandId" else ""
        val pArg = if (paintId != null) "$PAINT_ID_ARG=$paintId" else ""
        val joiner = if (bArg.isNotEmpty() && pArg.isNotEmpty()) "&" else ""
        return if (bArg.isEmpty() && pArg.isEmpty()) PAINT_ITEM_FORM_ROUTE else "$PAINT_ITEM_FORM_ROUTE?$bArg$joiner$pArg"
    }
}

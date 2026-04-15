package com.example.propaintersplastererspayment.app.navigation

object AppDestinations {
    // Core
    const val SPLASH_ROUTE          = "splash"
    const val SELECTION_ROUTE       = "selection"
    const val HOME_ROUTE            = "home"
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
}

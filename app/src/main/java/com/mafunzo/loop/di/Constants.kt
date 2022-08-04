package com.mafunzo.loop.di

object Constants {
    //Support constants
    const val SUPPORT_EMAIL = "mafunzoloop@gmail.com"

    const val FIREBASE_APP_SETTINGS = "app_settings"
    const val FIREBASE_APP_ACCOUNT_TYPES = "account_types"
    const val FIREBASE_APP_REQUEST_TYPES = "request_types"
    const val FIREBASE_APP_SCHOOLS = "schools"
    const val FIREBASE_APP_USERS = "users"
    const val FIREBASE_SCHOOL_ANNOUNCEMENTS = "announcements"
    const val FIREBASE_CALENDAR_EVENTS = "calendar_events"
    const val FIREBASE_REQUESTS = "requests"
    const val FIREBASE_TEACHERS = "teachers"
    const val FIREBASE_TEACHERS_COLLECTION = "collection"
    const val FIREBASE_SUBJECTS = "subjects"
    const val FIREBASE_SYSTEM_SETTINGS = "system_settings"

    //Parcelable keys
    const val ANNOUNCEMENT_STRING_KEY = "announcement"
    const val EVENT_STRING_KEY = "event"
    const val REQUEST_STRING_KEY = "request"
    const val TEACHER_STRING_KEY = "teacher"

    //request statuses
    const val REQUEST_STATUS_PENDING = "PENDING"
    const val REQUEST_STATUS_PROCESSING = "PROCESSING"
    const val REQUEST_STATUS_CANCELLED = "CANCELLED"
    const val REQUEST_STATUS_REJECTED = "REJECTED"
    const val REQUEST_STATUS_APPROVED = "APPROVED"
}
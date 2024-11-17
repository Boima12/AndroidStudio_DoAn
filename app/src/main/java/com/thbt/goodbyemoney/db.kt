package com.thbt.goodbyemoney

import com.thbt.goodbyemoney.models.Category
import com.thbt.goodbyemoney.models.Expense
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

val config = RealmConfiguration.create(schema = setOf(Expense::class, Category::class))
val db: Realm = Realm.open(config)
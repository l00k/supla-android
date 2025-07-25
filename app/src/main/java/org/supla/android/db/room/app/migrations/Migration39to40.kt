package org.supla.android.db.room.app.migrations
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity.Companion.COLUMN_ACTION
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity.Companion.COLUMN_CAPTION
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity.Companion.COLUMN_GLANCE_ID
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity.Companion.COLUMN_ID
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity.Companion.COLUMN_SUBJECT_ID
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity.Companion.COLUMN_SUBJECT_TYPE
import org.supla.android.db.room.SqlExecutor

val MIGRATION_39_40: Migration = object : Migration(39, 40), SqlExecutor {

  private val CREATE_WIDGET_CONFIGURATION_SQL =
    """
      CREATE TABLE ${WidgetConfigurationEntity.TABLE_NAME}
      (
        $COLUMN_ID INTEGER NOT NULL PRIMARY KEY,
        $COLUMN_SUBJECT_ID INTEGER NOT NULL,
        $COLUMN_SUBJECT_TYPE INTEGER NOT NULL,
        $COLUMN_CAPTION TEXT NOT NULL,
        $COLUMN_ACTION INTEGER NOT NULL,
        $COLUMN_PROFILE_ID INTEGER NOT NULL,
        $COLUMN_GLANCE_ID TEXT NOT NULL
      )
    """.trimIndent()

  override fun migrate(db: SupportSQLiteDatabase) {
    createWidgetConfigurationTable(db)
  }

  private fun createWidgetConfigurationTable(db: SupportSQLiteDatabase) {
    execSQL(db, CREATE_WIDGET_CONFIGURATION_SQL)
  }
}

package org.supla.android.data.source.local.entity.complex
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

import androidx.room.Embedded
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.usecases.channel.GetChannelStateUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.core.shared.data.model.general.SuplaFunction

data class WidgetConfigurationDataEntity(
  @Embedded(prefix = "channel_") val channelEntity: ChannelEntity?,
  @Embedded(prefix = "channel_group_") val groupEntity: ChannelGroupEntity?,
  @Embedded(prefix = "scene_") val sceneEntity: SceneEntity?,

  @Embedded(prefix = "profile_") val profileEntity: ProfileEntity,
  @Embedded(prefix = "configuration_") val widgetConfiguration: WidgetConfigurationEntity
) {

  val function: SuplaFunction
    get() =
      channelEntity?.function ?: groupEntity?.function ?: SuplaFunction.NONE

  private val state: ChannelState
    get() {
      val function = channelEntity?.function ?: groupEntity?.function
      return if (function != null) {
        GetChannelStateUseCase.getState(
          function = function,
          actionId = widgetConfiguration.action
        )
      } else {
        ChannelState(ChannelState.Value.NOT_USED)
      }
    }

  fun icon(getChannelIconUseCase: GetChannelIconUseCase, getSceneIconUseCase: GetSceneIconUseCase): ImageId =
    when (widgetConfiguration.subjectType) {
      SubjectType.GROUP -> getChannelIconUseCase.forState(groupEntity!!, state)
      SubjectType.SCENE -> getSceneIconUseCase(sceneEntity!!)
      SubjectType.CHANNEL -> getChannelIconUseCase.forState(channelEntity!!, state)
    }
}

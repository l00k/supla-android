package org.supla.android.features.details.thermostatdetail.general.ui
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

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.supla.android.R
import org.supla.android.core.shared.data.model.lists.resource
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.BaseViewProxy
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.formatting.LocalPercentageFormatter
import org.supla.android.data.model.temperature.TemperatureCorrection
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.features.details.thermostatdetail.general.ThermostatGeneralViewState
import org.supla.android.features.details.thermostatdetail.general.data.ThermostatProgramInfo
import org.supla.android.features.details.thermostatdetail.ui.ThermometersValues
import org.supla.android.features.details.thermostatdetail.ui.TimerHeader
import org.supla.android.images.ImageId
import org.supla.android.ui.lists.data.warning
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.LoadingScrim
import org.supla.android.ui.views.buttons.supla.SuplaButton
import org.supla.android.ui.views.buttons.supla.SuplaButtonDefaults
import org.supla.android.ui.views.thermostat.TemperatureControlButton
import org.supla.android.ui.views.tools.Shadow
import org.supla.android.ui.views.tools.ShadowOrientation
import org.supla.android.ui.views.tools.ThermostatControl
import org.supla.android.usecases.thermostat.MeasurementValue
import org.supla.core.shared.data.model.lists.ChannelIssueItem

interface ThermostatGeneralViewProxy : BaseViewProxy<ThermostatGeneralViewState> {
  fun heatingModeChanged()
  fun coolingModeChanged()
  fun setpointTemperatureChanged(heatPercentage: Float?, coolPercentage: Float?)
  fun changeSetpointTemperature(correction: TemperatureCorrection)
  fun turnOnOffClicked()
  fun manualModeClicked()
  fun weeklyScheduledModeClicked()
  fun getTemperatureText(minPercentage: Float?, maxPercentage: Float?, state: ThermostatGeneralViewState): StringProvider
  fun markChanging()
}

private val indicatorSize = 20.dp

@Composable
fun ThermostatDetail(viewProxy: ThermostatGeneralViewProxy) {
  val viewState by viewProxy.getViewState().collectAsState()

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .background(color = MaterialTheme.colorScheme.background)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
    ) {
      if (viewState.temperatures.isNotEmpty()) {
        ThermometersValues(temperatures = viewState.temperatures)
        Shadow(orientation = ShadowOrientation.STARTING_TOP)
      } else {
        // To avoid screen jumping
        EmptyThermometerValues()
        Shadow(orientation = ShadowOrientation.STARTING_TOP)
      }

      ThermostatView(viewState = viewState, viewProxy = viewProxy, modifier = Modifier.weight(1f))
    }

    if (viewState.loadingState.loading) {
      LoadingScrim()
    }
  }
}

context (ColumnScope)
@Composable
private fun ThermostatView(viewState: ThermostatGeneralViewState, viewProxy: ThermostatGeneralViewProxy, modifier: Modifier = Modifier) {
  BoxWithConstraints(modifier = modifier) {
    if (maxHeight < 298.dp) {
      if (viewState.isOff.not() && viewState.isAutoFunction && !viewState.programmedModeActive) {
        HeatingCoolingRowSmallScreen(viewState = viewState, viewProxy = viewProxy)
      }
      TemperatureControlRow(viewState, viewProxy, true)
      BottomButtonsForSmallScreen(viewState, viewProxy, modifier = Modifier.align(Alignment.BottomStart))
    } else if (maxHeight < 450.dp) {
      Column {
        Box(modifier = Modifier.weight(1f)) {
          if (viewState.isOff.not() && viewState.isAutoFunction && !viewState.programmedModeActive) {
            HeatingCoolingRowSmallScreen(viewState = viewState, viewProxy = viewProxy)
          }
          TemperatureControlRow(viewState, viewProxy, false)
          WarningsRow(viewState.issues, smallScreen = true, modifier = Modifier.align(Alignment.BottomStart))
        }
        BottomButtonsRow(viewState, viewProxy)
      }
    } else {
      Column {
        if (viewState.isOff.not() && viewState.isAutoFunction && !viewState.programmedModeActive) {
          HeatingCoolingRow(viewState = viewState, viewProxy = viewProxy)
        } else if (viewState.sensorIssue != null) {
          SensorIssueView(sensorIssue = viewState.sensorIssue)
        } else if (viewState.isOffline.not() && viewState.viewModelState?.timerEndDate != null) {
          TimerHeader(
            state = viewState,
            modifier = Modifier
              .fillMaxWidth()
              .height(80.dp)
          )
        } else if (viewState.temporaryProgramInfo.isNotEmpty()) {
          ProgramInfoRow(infos = viewState.temporaryProgramInfo)
        } else {
          Spacer(modifier = Modifier.height(80.dp))
        }
        TemperatureControlRow(viewState, viewProxy, false, modifier = Modifier.weight(1f))
        WarningsRow(viewState.issues)
        BottomButtonsRow(viewState, viewProxy)
      }
    }
  }
}

@Composable
private fun HeatingCoolingRow(viewState: ThermostatGeneralViewState, viewProxy: ThermostatGeneralViewProxy) {
  Row {
    Spacer(modifier = Modifier.weight(1f))
    HeatingIcon(active = viewState.heatingModeActive) { viewProxy.heatingModeChanged() }
    CoolingIcon(active = viewState.coolingModeActive) { viewProxy.coolingModeChanged() }
  }
}

@Composable
private fun HeatingCoolingRowSmallScreen(viewState: ThermostatGeneralViewState, viewProxy: ThermostatGeneralViewProxy) {
  Row {
    HeatingIcon(active = viewState.heatingModeActive) { viewProxy.heatingModeChanged() }
    Spacer(modifier = Modifier.weight(1f))
    CoolingIcon(active = viewState.coolingModeActive) { viewProxy.coolingModeChanged() }
  }
}

@Composable
private fun TemperatureControlRow(
  viewState: ThermostatGeneralViewState,
  viewProxy: ThermostatGeneralViewProxy,
  isSmallScreen: Boolean,
  modifier: Modifier = Modifier
) {
  ConstraintLayout(
    constraintSet = Constraints.build(viewState, isSmallScreen, Distance.toStatic()),
    modifier = modifier.fillMaxWidth()
  ) {
    val context = LocalContext.current

    ThermostatControl(
      modifier = Modifier
        .aspectRatio(1f)
        .layoutId(Constraints.CONTROL_WHEEL),
      mainTemperatureTextProvider = { min, max -> viewProxy.getTemperatureText(min, max, viewState)(context) },
      minTemperature = viewState.configMinTemperatureString,
      maxTemperature = viewState.configMaxTemperatureString,
      minSetpoint = viewState.setpointHeatTemperaturePercentage,
      maxSetpoint = viewState.setpointCoolTemperaturePercentage,
      currentValue = viewState.currentTemperaturePercentage,
      isHeating = viewState.showHeatingIndicator,
      isCooling = viewState.showCoolingIndicator,
      isOff = viewState.isOff,
      currentPower = viewState.currentPower,
      isOffline = viewState.isOffline,
      onPositionChangeStarted = { viewProxy.markChanging() },
      onPositionChangeEnded = { minPercentage, maxPercentage -> viewProxy.setpointTemperatureChanged(minPercentage, maxPercentage) }
    )

    ThermostatIndicators(viewState)
    ThermostatWheelControlButtons(viewState, viewProxy)
  }
}

@Composable
private fun ThermostatIndicators(viewState: ThermostatGeneralViewState) {
  if (viewState.showHeatingIndicator) {
    ThermostatHeatingIndicator(viewState = viewState)
  }
  if (viewState.showCoolingIndicator) {
    ThermostatCoolingIndicator(viewState = viewState)
  }
  viewState.pumpSwitchIcon?.let {
    Image(
      imageId = it,
      modifier = Modifier
        .size(32.dp)
        .layoutId(Constraints.PUMP_ICON)
    )
  }
  viewState.heatOrColdSourceSwitchIcon?.let {
    Image(
      imageId = it,
      modifier = Modifier
        .size(32.dp)
        .layoutId(Constraints.SOURCE_ICON)
    )
  }
}

@Composable
fun ThermostatHeatingIndicator(viewState: ThermostatGeneralViewState) {
  IndicatorIcon(
    iconRes = R.drawable.ic_heating,
    modifier = Modifier.layoutId(Constraints.HEATING_ICON)
  )
  viewState.currentPower?.let {
    Text(
      text = LocalPercentageFormatter.current.format(it),
      modifier = Modifier.layoutId(Constraints.HEATING_TEXT),
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onBackground
    )
  }
}

@Composable
fun ThermostatCoolingIndicator(viewState: ThermostatGeneralViewState) {
  IndicatorIcon(
    iconRes = R.drawable.ic_cooling,
    modifier = Modifier.layoutId(Constraints.COOLING_ICON)
  )

  viewState.currentPower?.let {
    Text(
      text = LocalPercentageFormatter.current.format(it),
      modifier = Modifier.layoutId(Constraints.COOLING_TEXT),
      style = MaterialTheme.typography.labelLarge
    )
  }
}

@Composable
private fun IndicatorIcon(iconRes: Int, modifier: Modifier = Modifier) {
  val transition = rememberInfiniteTransition(label = "Indicator alpha transition")
  val alpha by transition.animateFloat(
    initialValue = 1f,
    targetValue = 0.2f,
    animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
    label = "indicator alpha value"
  )

  Image(
    drawableId = iconRes,
    contentDescription = null,
    alpha = alpha,
    modifier = modifier.size(indicatorSize)
  )
}

@Composable
private fun ThermostatWheelControlButtons(
  viewState: ThermostatGeneralViewState,
  viewProxy: ThermostatGeneralViewProxy,
) {
  if ((!viewState.isOff || viewState.programmedModeActive) && !viewState.isOffline) {
    val color = if (viewState.viewModelState?.lastChangedHeat == false) {
      MaterialTheme.colorScheme.secondary
    } else {
      MaterialTheme.colorScheme.error
    }
    TemperatureControlButton(
      icon = R.drawable.ic_plus,
      color = color,
      disabled = viewState.canIncreaseTemperature.not(),
      onClick = { viewProxy.changeSetpointTemperature(TemperatureCorrection.UP) },
      modifier = Modifier.layoutId(Constraints.INCREASE_BUTTON)
    )
    TemperatureControlButton(
      icon = R.drawable.ic_minus,
      color = color,
      disabled = viewState.canDecreaseTemperature.not(),
      onClick = { viewProxy.changeSetpointTemperature(TemperatureCorrection.DOWN) },
      modifier = Modifier.layoutId(Constraints.DECREASE_BUTTON)
    )
  }
}

@Composable
private fun HeatingIcon(active: Boolean, onClick: () -> Unit) {
  SuplaButton(
    iconRes = R.drawable.ic_heat,
    onClick = onClick,
    modifier = Modifier
      .padding(
        top = dimensionResource(id = R.dimen.distance_default),
        end = dimensionResource(id = R.dimen.distance_default),
        bottom = dimensionResource(id = R.dimen.distance_default),
        start = dimensionResource(id = R.dimen.distance_default)
      )
      .width(dimensionResource(id = R.dimen.button_default_size)),
    pressed = active,
    colors = SuplaButtonDefaults.errorColors()
  )
}

@Composable
private fun CoolingIcon(active: Boolean, onClick: () -> Unit) {
  SuplaButton(
    iconRes = R.drawable.ic_cool,
    onClick = onClick,
    modifier = Modifier
      .padding(
        top = dimensionResource(id = R.dimen.distance_default),
        end = dimensionResource(id = R.dimen.distance_default),
        bottom = dimensionResource(id = R.dimen.distance_default)
      )
      .width(dimensionResource(id = R.dimen.button_default_size)),
    pressed = active,
    colors = SuplaButtonDefaults.secondaryColors()
  )
}

@Composable
private fun WarningsRow(warnings: List<ChannelIssueItem>, modifier: Modifier = Modifier, smallScreen: Boolean = false) {
  val defaultPadding = dimensionResource(id = R.dimen.distance_default)
  Column(
    modifier = modifier.padding(start = defaultPadding, end = defaultPadding),
    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_tiny))
  ) {
    warnings.forEach {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_small))
      ) {
        Image(
          drawableId = it.icon.resource,
          contentDescription = null,
          modifier = Modifier.size(dimensionResource(id = R.dimen.channel_warning_image_size))
        )
        Text(
          text = it.message(LocalContext.current),
          style = MaterialTheme.typography.bodyMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      if (smallScreen) {
        return
      }
    }
  }
}

@Composable
private fun BottomButtonsRow(viewState: ThermostatGeneralViewState, viewProxy: ThermostatGeneralViewProxy, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .padding(
        start = Distance.default,
        end = Distance.default,
        bottom = Distance.default,
        top = Distance.tiny
      ),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    PowerButton(
      isOff = viewState.isOff && viewState.programmedModeActive.not(),
      disabled = viewState.isOffline
    ) { viewProxy.turnOnOffClicked() }
    SuplaButton(
      text = stringResource(id = R.string.thermostat_detail_mode_manual),
      modifier = Modifier.weight(0.5f),
      disabled = viewState.isOffline,
      pressed = viewState.manualModeActive
    ) {
      if (viewState.isOffline.not() && viewState.manualModeActive.not()) {
        viewProxy.manualModeClicked()
      }
    }
    SuplaButton(
      text = stringResource(id = R.string.thermostat_detail_mode_weekly_schedule),
      modifier = Modifier.weight(0.5f),
      disabled = viewState.isOffline,
      pressed = viewState.programmedModeActive
    ) {
      if (viewState.isOffline.not() && (viewState.programmedModeActive.not() || viewState.temporaryChangeActive)) {
        viewProxy.weeklyScheduledModeClicked()
      }
    }
  }
}

@Composable
private fun BottomButtonsForSmallScreen(
  viewState: ThermostatGeneralViewState,
  viewProxy: ThermostatGeneralViewProxy,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .padding(
        start = Distance.default,
        end = Distance.default,
        bottom = Distance.default,
        top = Distance.tiny
      ),
    verticalArrangement = Arrangement.spacedBy(Distance.small)
  ) {
    PowerButton(
      isOff = viewState.isOff && viewState.programmedModeActive.not(),
      disabled = viewState.isOffline
    ) { viewProxy.turnOnOffClicked() }
    SuplaButton(
      iconRes = R.drawable.ic_manual,
      disabled = viewState.isOffline,
      pressed = viewState.manualModeActive
    ) {
      if (viewState.isOffline.not() && viewState.manualModeActive.not()) {
        viewProxy.manualModeClicked()
      }
    }
    SuplaButton(
      iconRes = R.drawable.ic_schedule,
      disabled = viewState.isOffline,
      pressed = viewState.programmedModeActive
    ) {
      if (viewState.isOffline.not() && (viewState.programmedModeActive.not() || viewState.temporaryChangeActive)) {
        viewProxy.weeklyScheduledModeClicked()
      }
    }
  }
}

@Composable
private fun PowerButton(isOff: Boolean, disabled: Boolean, onClick: () -> Unit) {
  val color = if (isOff) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
  SuplaButton(
    iconRes = R.drawable.ic_power_button,
    disabled = disabled,
    colors = SuplaButtonDefaults.buttonColors(content = color, contentPressed = color),
    onClick = onClick
  )
}

@Composable
private fun EmptyThermometerValues() = Spacer(
  modifier = Modifier
    .fillMaxWidth()
    .background(color = MaterialTheme.colorScheme.surface)
    .height(dimensionResource(id = R.dimen.detail_top_component))
)

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    ThermostatDetail(
      PreviewProxy(
        ThermostatGeneralViewState(
          showHeatingIndicator = true,
          currentPower = 18f,
          pumpSwitchIcon = ImageId(R.drawable.fnc_pump_switch_on),
          heatOrColdSourceSwitchIcon = ImageId(R.drawable.fnc_heat_or_cold_source_switch_off)
        )
      )
    )
  }
}

@Preview
@Composable
private fun PreviewCooling() {
  SuplaTheme {
    ThermostatDetail(
      PreviewProxy(
        ThermostatGeneralViewState(
          showCoolingIndicator = true,
          currentPower = 18f,
          pumpSwitchIcon = ImageId(R.drawable.fnc_pump_switch_off),
          heatOrColdSourceSwitchIcon = ImageId(R.drawable.fnc_heat_or_cold_source_switch_on)
        )
      )
    )
  }
}

@Preview
@Composable
private fun PreviewTemporaryOverride() {
  SuplaTheme {
    ThermostatDetail(
      PreviewProxy(
        ThermostatGeneralViewState(
          temporaryChangeActive = true,
          temporaryProgramInfo = listOf(
            ThermostatProgramInfo(
              ThermostatProgramInfo.Type.CURRENT,
              { "vor 7 hours 10 min." },
              R.drawable.ic_heat,
              R.color.red,
              { "22.7" },
              true
            ),
            ThermostatProgramInfo(
              ThermostatProgramInfo.Type.NEXT,
              null,
              R.drawable.ic_power_button,
              R.color.on_surface_variant,
              { "Turn off" }
            )
          ),
          coolingModeActive = true,
          isAutoFunction = true,
          issues = listOf(
            ChannelIssueItem.warning(R.string.thermostat_detail_mode_manual)
          )
        )
      )
    )
  }
}

@Preview
@Composable
private fun PreviewSmall() {
  SuplaTheme {
    Box(modifier = Modifier.height(500.dp)) {
      ThermostatDetail(
        PreviewProxy(
          ThermostatGeneralViewState(
            issues = listOf(
              ChannelIssueItem.warning(R.string.thermostat_detail_mode_manual)
            )
          )
        )
      )
    }
  }
}

@Preview
@Composable
private fun PreviewVerySmall() {
  SuplaTheme {
    Box(modifier = Modifier.height(381.dp)) {
      ThermostatDetail(
        PreviewProxy(
          ThermostatGeneralViewState(
            showHeatingIndicator = true,
            showCoolingIndicator = true,
            currentPower = 18f,
            pumpSwitchIcon = ImageId(R.drawable.fnc_pump_switch_on),
            heatOrColdSourceSwitchIcon = ImageId(R.drawable.fnc_heat_or_cold_source_switch_off)
          )
        )
      )
    }
  }
}

private class PreviewProxy(private var initialState: ThermostatGeneralViewState = ThermostatGeneralViewState(isAutoFunction = true)) :
  ThermostatGeneralViewProxy {
  override fun getViewState(): StateFlow<ThermostatGeneralViewState> =
    MutableStateFlow(
      value = initialState.copy(
        loadingState = LoadingTimeoutManager.LoadingState(loading = false),
        temperatures = listOf(
          MeasurementValue(
            remoteId = 123,
            imageId = ImageId(R.drawable.fnc_thermometer),
            value = "12.3"
          )
        )
      )
    )

  override fun heatingModeChanged() {}

  override fun coolingModeChanged() {}

  override fun setpointTemperatureChanged(heatPercentage: Float?, coolPercentage: Float?) {}

  override fun changeSetpointTemperature(correction: TemperatureCorrection) {}

  override fun turnOnOffClicked() {}

  override fun manualModeClicked() {}

  override fun weeklyScheduledModeClicked() {}

  override fun getTemperatureText(
    minPercentage: Float?,
    maxPercentage: Float?,
    state: ThermostatGeneralViewState
  ): StringProvider = { "25,0°" }

  override fun markChanging() {}
}

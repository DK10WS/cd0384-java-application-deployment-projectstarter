package com.udacity.catpoint;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

import com.udacity.catpoint.application.StatusListener;
import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.data.Sensor;
import com.udacity.catpoint.data.SensorType;
import com.udacity.catpoint.image.ImageService;
import com.udacity.catpoint.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
// ALl test given in step 3
public class AppTest {

    @Mock
    private SecurityRepository securityRepository;

    @Mock
    private ImageService imageService;

    private SecurityService securityService;

    @BeforeEach
    void setup() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    @Test
    void activatingSensorWhenSystemIsClearShouldSetPendingAlarm() {
        Sensor frontDoor = new Sensor("Front Door", SensorType.DOOR);

        when(securityRepository.getAlarmStatus()).thenReturn(
            AlarmStatus.NO_ALARM
        );

        securityService.changeSensorActivationStatus(frontDoor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    void activatingSensorWhileAlarmPendingShouldTriggerAlarm() {
        Sensor windowSensor = new Sensor("Window Sensor", SensorType.WINDOW);

        when(securityRepository.getAlarmStatus()).thenReturn(
            AlarmStatus.PENDING_ALARM
        );

        securityService.changeSensorActivationStatus(windowSensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void sensorChangesShouldBeIgnoredWhenAlarmAlreadyActive() {
        Sensor garageSensor = new Sensor("Garage Sensor", SensorType.DOOR);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(garageSensor, false);

        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @Test
    void inactiveSensorShouldNotChangeAnythingWhenSystemAlreadyClear() {
        Sensor backDoor = new Sensor("Back Door", SensorType.DOOR);

        when(securityRepository.getAlarmStatus()).thenReturn(
            AlarmStatus.NO_ALARM
        );

        securityService.changeSensorActivationStatus(backDoor, false);

        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @Test
    void deactivatingLastActiveSensorShouldClearAlarmState() {
        Sensor livingRoomWindow = new Sensor(
            "Living Room Window",
            SensorType.WINDOW
        );

        livingRoomWindow.setActive(true);

        when(securityRepository.getAlarmStatus()).thenReturn(
            AlarmStatus.PENDING_ALARM
        );

        when(securityRepository.getSensors()).thenReturn(
            java.util.Set.of(livingRoomWindow)
        );

        securityService.changeSensorActivationStatus(livingRoomWindow, false);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void disarmingSystemShouldResetAlarmState() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void activeSensorWhilePendingShouldTriggerAlarm() {
        Sensor sensor = new Sensor("Front Door", SensorType.DOOR);

        sensor.setActive(true);

        when(securityRepository.getAlarmStatus()).thenReturn(
            AlarmStatus.PENDING_ALARM
        );

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void catDetectedWhileArmedHomeShouldTriggerAlarm() {
        when(securityRepository.getArmingStatus()).thenReturn(
            ArmingStatus.ARMED_HOME
        );

        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        securityService.processImage(null);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void noCatAndNoActiveSensorsShouldResetAlarm() {
        Sensor sensor = new Sensor("Living Room", SensorType.WINDOW);

        sensor.setActive(false);

        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(
            false
        );

        when(securityRepository.getSensors()).thenReturn(
            java.util.Set.of(sensor)
        );

        securityService.processImage(null);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void armingSystemShouldDeactivateSensors() {
        Sensor sensor = new Sensor("Garage", SensorType.DOOR);

        sensor.setActive(true);

        when(securityRepository.getSensors()).thenReturn(
            java.util.Set.of(sensor)
        );

        securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);

        assertFalse(sensor.getActive());
    }

    @Test
    void armingHomeWhileCatDetectedShouldTriggerAlarm() {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        securityService.processImage(null);

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    // Junit 5 test was mentioned using Parameterized test could only think of this
    // if there are any more please do tell
    @ParameterizedTest
    @EnumSource(
        value = ArmingStatus.class,
        names = {"ARMED_HOME", "ARMED_AWAY"}
    )
    void armingSystemShouldDeactivateAllSensors(ArmingStatus status) {
        Sensor sensor = new Sensor("DrawingRoom Window", SensorType.WINDOW);

        sensor.setActive(true);

        when(securityRepository.getSensors()).thenReturn(
            java.util.Set.of(sensor)
        );

        securityService.setArmingStatus(status);

        assertFalse(sensor.getActive());
    }

    @Test
    void armedHomeWithExistingCatShouldTriggerAlarm() {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        securityService.processImage(null);

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void noCatAndNoActiveSensorsShouldSetNoAlarm() {
        Sensor sensor = new Sensor("Window", SensorType.WINDOW);

        sensor.setActive(false);

        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(
            false
        );

        when(securityRepository.getSensors()).thenReturn(
            java.util.Set.of(sensor)
        );

        securityService.processImage(null);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void noCatButActiveSensorShouldNotResetAlarm() {
        Sensor sensor = new Sensor("Door", SensorType.DOOR);

        sensor.setActive(true);

        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(
            false
        );

        when(securityRepository.getSensors()).thenReturn(
            java.util.Set.of(sensor)
        );

        securityService.processImage(null);

        verify(securityRepository, never()).setAlarmStatus(
            AlarmStatus.NO_ALARM
        );
    }

    @Test
    void catDetectedWhileDisarmedShouldNotTriggerAlarm() {
        when(securityRepository.getArmingStatus()).thenReturn(
            ArmingStatus.DISARMED
        );

        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        securityService.processImage(null);

        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.ALARM);
    }

    //SInce these are not needed to be tested
    @Test
    void removeStatusListenerShouldStopNotifications() {
        StatusListener listener = mock(StatusListener.class);

        securityService.addStatusListener(listener);

        securityService.removeStatusListener(listener);

        securityService.setAlarmStatus(AlarmStatus.ALARM);

        verify(listener, never()).notify(any());
    }
}

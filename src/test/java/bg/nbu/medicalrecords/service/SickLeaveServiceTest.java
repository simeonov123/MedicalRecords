package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.*;
import bg.nbu.medicalrecords.dto.SickLeaveDto;
import bg.nbu.medicalrecords.dto.UpdateSickLeaveDto;
import bg.nbu.medicalrecords.exception.DoctorNotAssignedToAppointmentException;
import bg.nbu.medicalrecords.exception.SickLeaveNotFoundException;
import bg.nbu.medicalrecords.repository.SickLeaveRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SickLeaveServiceTest {

    @Mock
    private SickLeaveRepository sickLeaveRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private DoctorService doctorService;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private SickLeaveService sickLeaveService;

    @Test
    void createSickLeave_Success() {
        // Arrange
        Long appointmentId = 1L;
        SickLeaveDto sickLeaveDto = new SickLeaveDto(
                null, // ID not required for creation
                "Reason",
                LocalDate.now(),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5),
                null,
                null
        );

        User currentUser = new User();
        currentUser.setRole("doctor");

        Doctor doctor = new Doctor();
        doctor.setId(2L);

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setDoctor(doctor);
        appointment.setSickLeaves(new ArrayList<>());

        SickLeave sickLeave = new SickLeave();
        sickLeave.setId(3L);
        sickLeave.setReason(sickLeaveDto.getReason());
        sickLeave.setTodayDate(sickLeaveDto.getTodayDate());
        sickLeave.setStartDate(sickLeaveDto.getStartDate());
        sickLeave.setEndDate(sickLeaveDto.getEndDate());
        sickLeave.setAppointment(appointment);

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(appointmentService.findById(appointmentId)).thenReturn(appointment);
        when(doctorService.findByPrincipal()).thenReturn(doctor);
        when(sickLeaveRepository.save(any(SickLeave.class))).thenReturn(sickLeave);

        // Act
        SickLeave result = sickLeaveService.createSickLeave(appointmentId, sickLeaveDto);

        // Assert
        assertNotNull(result);
        assertEquals(sickLeave.getReason(), result.getReason());
        verify(appointmentService, times(1)).save(appointment);
    }

    @Test
    void createSickLeave_DoctorNotAssigned() {
        // Arrange
        Long appointmentId = 1L;
        SickLeaveDto sickLeaveDto = new SickLeaveDto(
                null,
                "Reason",
                LocalDate.now(),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5),
                null,
                null
        );

        User currentUser = new User();
        currentUser.setRole("doctor");

        Doctor doctor = new Doctor();
        doctor.setId(2L);

        Doctor otherDoctor = new Doctor();
        otherDoctor.setId(3L);

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setDoctor(otherDoctor);

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(appointmentService.findById(appointmentId)).thenReturn(appointment);
        when(doctorService.findByPrincipal()).thenReturn(doctor);

        // Act & Assert
        assertThrows(DoctorNotAssignedToAppointmentException.class, () ->
                sickLeaveService.createSickLeave(appointmentId, sickLeaveDto));

        verify(sickLeaveRepository, never()).save(any(SickLeave.class));
    }

    @Test
    void updateSickLeave_Success() {
        // Arrange
        Long appointmentId = 1L;
        Long sickLeaveId = 2L;
        UpdateSickLeaveDto updateSickLeaveDto = new UpdateSickLeaveDto();
        updateSickLeaveDto.setReason("Updated Reason");
        updateSickLeaveDto.setTodayDate(LocalDate.now());
        updateSickLeaveDto.setStartDate(LocalDate.now().minusDays(1));
        updateSickLeaveDto.setEndDate(LocalDate.now().plusDays(5));

        User currentUser = new User();
        currentUser.setRole("doctor");

        Doctor doctor = new Doctor();
        doctor.setId(2L);

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setDoctor(doctor);

        SickLeave sickLeave = new SickLeave();
        sickLeave.setId(sickLeaveId);
        sickLeave.setAppointment(appointment);

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(appointmentService.findById(appointmentId)).thenReturn(appointment);
        when(doctorService.findByPrincipal()).thenReturn(doctor);
        when(sickLeaveRepository.findById(sickLeaveId)).thenReturn(Optional.of(sickLeave));
        when(sickLeaveRepository.save(any(SickLeave.class))).thenReturn(sickLeave);

        // Act
        SickLeave result = sickLeaveService.updateSickLeave(appointmentId, updateSickLeaveDto, sickLeaveId);

        // Assert
        assertNotNull(result);
        assertEquals(updateSickLeaveDto.getReason(), result.getReason());
        verify(appointmentService, times(1)).save(appointment);
    }

    @Test
    void deleteSickLeave_Success() {
        // Arrange
        Long appointmentId = 1L;
        Long sickLeaveId = 2L;

        User currentUser = new User();
        currentUser.setRole("doctor");

        Doctor doctor = new Doctor();
        doctor.setId(2L);

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setDoctor(doctor);

        SickLeave sickLeave = new SickLeave();
        sickLeave.setId(sickLeaveId);
        sickLeave.setAppointment(appointment);

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(appointmentService.findById(appointmentId)).thenReturn(appointment);
        when(doctorService.findByPrincipal()).thenReturn(doctor);
        when(sickLeaveRepository.findById(sickLeaveId)).thenReturn(Optional.of(sickLeave));

        // Act
        sickLeaveService.deleteSickLeave(sickLeaveId, appointmentId);

        // Assert
        verify(sickLeaveRepository, times(1)).delete(sickLeave);
        verify(appointmentService, times(1)).save(appointment);
    }

    @Test
    void findAllSickLeaves_Success() {
        // Arrange
        List<SickLeave> sickLeaves = new ArrayList<>();
        sickLeaves.add(new SickLeave());
        sickLeaves.add(new SickLeave());

        when(sickLeaveRepository.findAll()).thenReturn(sickLeaves);

        // Act
        List<SickLeave> result = sickLeaveService.findAllSickLeaves();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }
}

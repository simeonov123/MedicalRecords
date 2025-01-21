package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.*;
import bg.nbu.medicalrecords.dto.*;
import bg.nbu.medicalrecords.exception.StatisticsServiceException;
import bg.nbu.medicalrecords.util.MappingUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    public final DiagnosisService diagnosisService;
    public final PatientService patientService;
    public final UserService userService;
    public final DoctorService doctorService;
    public final AppointmentService appointmentService;
    public final SickLeaveService sickLeaveService;

    public StatisticsService(DiagnosisService diagnosisService, PatientService patientService, UserService userService, DoctorService doctorService, AppointmentService appointmentService, SickLeaveService sickLeaveService) {
        this.diagnosisService = diagnosisService;
        this.patientService = patientService;
        this.userService = userService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.sickLeaveService = sickLeaveService;
    }

    public List<String> getUniqueDiagnosis() {
        try {
            return diagnosisService.getUniqueDiagnosis();
        } catch (Exception e) {
            throw new StatisticsServiceException("Failed to get unique diagnosis", e);
        }
    }

    public DiagnosisStatisticsDto getDiagnosisLeaderboard() {
        try {
            List<String> uniqueDiagnosisStatements = diagnosisService.getUniqueDiagnosis();
            List<DiagnosisDetailsDto> diagnosisDetailsList = new ArrayList<>();

            for (String statement : uniqueDiagnosisStatements) {
                List<Diagnosis> diagnoses = diagnosisService.findByStatement(statement);
                long count = diagnoses.size();
                long totalDiagnoses = diagnosisService.count();
                long totalPatients = patientService.count();
                long patientsWithDiagnosis = diagnoses.stream().map(Diagnosis::getAppointment).map(Appointment::getPatient).distinct().count();

                Diagnosis firstDiagnosis = diagnoses.stream().min(Comparator.comparing(Diagnosis::getDiagnosedDate)).orElse(null);
                Diagnosis lastDiagnosis = diagnoses.stream().max(Comparator.comparing(Diagnosis::getDiagnosedDate)).orElse(null);

                DiagnosisDetailsDto detailsDto = new DiagnosisDetailsDto();
                detailsDto.setStatement(statement);
                detailsDto.setCount(count);
                detailsDto.setPercentageOfAllDiagnoses((count * 100) / totalDiagnoses);
                detailsDto.setPercentageOfAllPatients((patientsWithDiagnosis * 100) / totalPatients);
                detailsDto.setDoctorNameOfFirstDiagnosis(firstDiagnosis != null ? firstDiagnosis.getAppointment().getDoctor().getName() : null);
                detailsDto.setDateOfFirstDiagnosis(firstDiagnosis != null ? firstDiagnosis.getDiagnosedDate() : null);
                detailsDto.setDateOfLastDiagnosis(lastDiagnosis != null ? lastDiagnosis.getDiagnosedDate() : null);

                diagnosisDetailsList.add(detailsDto);
            }

            diagnosisDetailsList.sort(Comparator.comparingLong(DiagnosisDetailsDto::getCount).reversed());

            DiagnosisStatisticsDto statisticsDto = new DiagnosisStatisticsDto();
            statisticsDto.setDiagnosisDetails(diagnosisDetailsList);

            return statisticsDto;
        } catch (Exception e) {
            throw new StatisticsServiceException("Failed to get diagnosis leaderboard", e);
        }
    }

    public List<PatientDto> findAllByStatement(String diagnosisStatement) {
        try {
            List<Appointment> appointments = diagnosisService.findByStatement(diagnosisStatement).stream()
                    .map(Diagnosis::getAppointment)
                    .toList();

            List<Patient> patients = appointments.stream()
                    .map(Appointment::getPatient)
                    .toList();

            patients = patients.stream().distinct().toList();

            List<PatientDto> patientDtos = new ArrayList<>();
            for (Patient patient : patients) {
                User user = userService.findByKeycloakUserId(patient.getKeycloakUserId());
                patientDtos.add(MappingUtils.mapToPatientDto(patient, user));
            }

            return patientDtos;
        } catch (Exception e) {
            throw new StatisticsServiceException("Failed to find all patients by diagnosis statement", e);
        }
    }

    public List<DoctorPatientCountDto> getDoctorsWithPatientCount() {
        try {
            List<Doctor> doctors = doctorService.findAll();
            List<DoctorPatientCountDto> doctorPatientCountDtos = new ArrayList<>();

            for (Doctor doctor : doctors) {
                List<PatientDto> patients = patientService.findAllByPrimaryDoctorId(doctor.getId());
                DoctorPatientCountDto doctorPatientCountDto = new DoctorPatientCountDto();
                doctorPatientCountDto.setDoctorName(doctor.getName());
                doctorPatientCountDto.setCount((long) patients.size());
                doctorPatientCountDtos.add(doctorPatientCountDto);
            }

            return doctorPatientCountDtos;
        } catch (Exception e) {
            throw new StatisticsServiceException("Failed to get doctors with patient count", e);
        }
    }

    public List<DoctorAppointmentsCount> getDoctorsWithAppointmentsCount() {
        try {
            List<Doctor> doctors = doctorService.findAll();
            List<DoctorAppointmentsCount> doctorAppointmentsCounts = new ArrayList<>();

            for (Doctor doctor : doctors) {
                List<Appointment> appointments = appointmentService.findAllByDoctorId(doctor.getId());
                DoctorAppointmentsCount doctorAppointmentsCount = new DoctorAppointmentsCount();
                doctorAppointmentsCount.setDoctorName(doctor.getName());
                doctorAppointmentsCount.setCount((long) appointments.size());
                doctorAppointmentsCounts.add(doctorAppointmentsCount);
            }

            return doctorAppointmentsCounts;
        } catch (Exception e) {
            throw new StatisticsServiceException("Failed to get doctors with appointments count", e);
        }
    }

    public List<DoctorsThatHaveAppointmentsInPeriod> getDoctorsWithAppointmentsInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<Doctor> doctors = doctorService.findAll();
            List<DoctorsThatHaveAppointmentsInPeriod> doctorsThatHaveAppointmentsInPeriodList = new ArrayList<>();

            for (Doctor doctor : doctors) {
                List<Appointment> appointments = appointmentService.findAllByDoctorId(doctor.getId());
                List<Appointment> appointmentsInPeriod = appointments.stream()
                        .filter(appointment -> appointment.getAppointmentDateTime().isAfter(startDate) && appointment.getAppointmentDateTime().isBefore(endDate))
                        .toList();

                if (appointmentsInPeriod.isEmpty()) {
                    continue;
                }

                DoctorsThatHaveAppointmentsInPeriod doctorsThatHaveAppointmentsInPeriod = new DoctorsThatHaveAppointmentsInPeriod();
                doctorsThatHaveAppointmentsInPeriod.setDoctorName(doctor.getName());
                doctorsThatHaveAppointmentsInPeriod.setDoctorId(doctor.getId());
                doctorsThatHaveAppointmentsInPeriod.setStartDate(startDate.toLocalDate());
                doctorsThatHaveAppointmentsInPeriod.setEndDate(endDate.toLocalDate());

                doctorsThatHaveAppointmentsInPeriodList.add(doctorsThatHaveAppointmentsInPeriod);
            }

            return doctorsThatHaveAppointmentsInPeriodList;
        } catch (Exception e) {
            throw new StatisticsServiceException("Failed to get doctors with appointments in period", e);
        }
    }

    public MostSickLeavesMonthData getMostSickLeavesMonthData() {
        try {
            List<SickLeave> sickLeaves = sickLeaveService.findAllSickLeaves();
            int currentYear = LocalDateTime.now().getYear();

            Map<Integer, Long> sickLeavesCountByMonth = sickLeaves.stream()
                    .filter(sickLeave -> sickLeave.getStartDate().getYear() == currentYear)
                    .collect(Collectors.groupingBy(sickLeave -> sickLeave.getStartDate().getMonthValue(), Collectors.counting()));

            int mostSickLeavesMonth = sickLeavesCountByMonth.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(0);

            long mostSickLeavesCount = sickLeavesCountByMonth.getOrDefault(mostSickLeavesMonth, 0L);

            String monthName = switch (mostSickLeavesMonth) {
                case 1 -> "January";
                case 2 -> "February";
                case 3 -> "March";
                case 4 -> "April";
                case 5 -> "May";
                case 6 -> "June";
                case 7 -> "July";
                case 8 -> "August";
                case 9 -> "September";
                case 10 -> "October";
                case 11 -> "November";
                case 12 -> "December";
                default -> null;
            };

            List<Appointment> allAppointmentsForMostSickLeavesMonth = appointmentService.findAll().stream()
                    .filter(appointment -> appointment.getAppointmentDateTime().getMonthValue() == mostSickLeavesMonth && appointment.getAppointmentDateTime().getYear() == currentYear)
                    .toList();

            List<String> diagnosisStatements = allAppointmentsForMostSickLeavesMonth.stream()
                    .flatMap(appointment -> appointment.getDiagnoses().stream())
                    .map(Diagnosis::getStatement)
                    .toList();

            String mostCommonDiagnosis = diagnosisStatements.stream()
                    .collect(Collectors.groupingBy(statement -> statement, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            long uniquePatientsCount = allAppointmentsForMostSickLeavesMonth.stream()
                    .map(Appointment::getPatient)
                    .distinct()
                    .count();

            MostSickLeavesMonthData mostSickLeavesMonthData = new MostSickLeavesMonthData();
            mostSickLeavesMonthData.setMonthName(monthName);
            mostSickLeavesMonthData.setSickLeavesCount((int) mostSickLeavesCount);
            mostSickLeavesMonthData.setAppointmentsThatMonthCount(allAppointmentsForMostSickLeavesMonth.size());
            mostSickLeavesMonthData.setUniquePatientsCount((int) uniquePatientsCount);
            mostSickLeavesMonthData.setMostCommonDiagnosisThatMonth(mostCommonDiagnosis);

            return mostSickLeavesMonthData;
        } catch (Exception e) {
            throw new StatisticsServiceException("Failed to get most sick leaves month data", e);
        }
    }

    public List<DoctorsSickLeavesLeaderboardDto> getDoctorsSickLeavesLeaderboard() {
        try {
            List<Doctor> doctors = doctorService.findAll();
            List<DoctorsSickLeavesLeaderboardDto> leaderboard = new ArrayList<>();
            List<Appointment> appointments = appointmentService.findAll();

            for (Doctor doctor : doctors) {
                long sickLeavesCount = appointments.stream()
                        .filter(appointment -> appointment.getDoctor().getId().equals(doctor.getId()))
                        .flatMap(appointment -> appointment.getSickLeaves().stream())
                        .count();

                DoctorsSickLeavesLeaderboardDto dto = new DoctorsSickLeavesLeaderboardDto();
                dto.setName(doctor.getName());
                dto.setSpecialties(doctor.getSpecialties());
                dto.setPrimaryCare(doctor.isPrimaryCare());
                dto.setSickLeavesCount((int) sickLeavesCount);
                leaderboard.add(dto);
            }

            leaderboard.sort(Comparator.comparingInt(DoctorsSickLeavesLeaderboardDto::getSickLeavesCount).reversed());

            return leaderboard;
        } catch (Exception e) {
            throw new StatisticsServiceException("Failed to get doctors sick leaves leaderboard", e);
        }
    }
}
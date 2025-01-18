package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.*;
import bg.nbu.medicalrecords.dto.*;
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
        return diagnosisService.getUniqueDiagnosis();
    }

        //This method must return a list of DiagnosisStatisticsDto objects
        //first we need to get all of the distinct diagnosis statements
        //then based on that we must sort them by the number of appointments with that diagnosis
        //for each unique diagnosis statement we must create a DiagnosisDetailsDto object
        //we must set each DiagnosisDetailsDto object's statement field to the unique diagnosis statement as well as
        //         Long count - the number of diagnosis appointments with that statement;
        //        Long percentageOfAllDiagnoses - the percentage of all diagnosis appointments with that statement compared to all diagnosis appointments;
        //        Long percentageOfAllPatients - the percentage of all patients that have been diagnosed with that statement compared to all patients that have been diagnosed;
        //        String doctorNameOfFirstDiagnosis - the name of the doctor that made the first diagnosis with that statement;
        //        LocalDateTime dateOfFirstDiagnosis - the date of the first diagnosis with that statement;
        //        LocalDateTime dateOfLastDiagnosis - the date of the last diagnosis with that statement;

        //after we have created all DiagnosisDetailsDto objects we must sort them by the count field in descending order
        //and return them as a list of DiagnosisStatisticsDto objects
        public DiagnosisStatisticsDto getDiagnosisLeaderboard() {
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
        }

    public List<PatientDto> findAllByStatement(String diagnosisStatement) {

        List<Appointment> appointments = diagnosisService.findByStatement(diagnosisStatement).stream()
                .map(Diagnosis::getAppointment)
                .toList();



        List<Patient> patients = appointments.stream()
                .map(Appointment::getPatient)
                .toList();

        //now that we have the patients with the diagnosis we only want distinct patients
        patients = patients.stream().distinct().toList();

        List<PatientDto> patientDtos = new ArrayList<>();
        for (Patient patient : patients) {
            User user = userService.findByKeycloakUserId(patient.getKeycloakUserId());
            patientDtos.add(MappingUtils.mapToPatientDto(patient, user));
        }

        return patientDtos;
    }

    public List<DoctorPatientCountDto> getDoctorsWithPatientCount() {
        //get all doctors
        List<Doctor> doctors = doctorService.findAll();

        //for every doctor get the number of patient by using the doctor id to get patients with that doctor
        //then create a DoctorPatientCountDto object with the doctor's name and the number of patients
        List<DoctorPatientCountDto> doctorPatientCountDtos = new ArrayList<>();

        for (Doctor doctor : doctors) {
            List<PatientDto> patients = patientService.findAllByPrimaryDoctorId(doctor.getId());
            DoctorPatientCountDto doctorPatientCountDto = new DoctorPatientCountDto();
            doctorPatientCountDto.setDoctorName(doctor.getName());
            doctorPatientCountDto.setCount((long) patients.size());
            doctorPatientCountDtos.add(doctorPatientCountDto);
        }

        return doctorPatientCountDtos;
    }

    public List<DoctorAppointmentsCount> getDoctorsWithAppointmentsCount() {
        //get all doctors
        List<Doctor> doctors = doctorService.findAll();

        //for every doctor get the number of appointments by using the doctor id to get appointments with that doctor
        //then create a DoctorAppointmentsCount object with the doctor's name and the number of appointments
        List<DoctorAppointmentsCount> doctorAppointmentsCounts = new ArrayList<>();

        for (Doctor doctor : doctors) {
            List<Appointment> appointments = appointmentService.findAllByDoctorId(doctor.getId());
            DoctorAppointmentsCount doctorAppointmentsCount = new DoctorAppointmentsCount();
            doctorAppointmentsCount.setDoctorName(doctor.getName());
            doctorAppointmentsCount.setCount((long) appointments.size());
            doctorAppointmentsCounts.add(doctorAppointmentsCount);
        }

        return doctorAppointmentsCounts;
    }

    public List<DoctorsThatHaveAppointmentsInPeriod> getDoctorsWithAppointmentsInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        //get all doctors
        List<Doctor> doctors = doctorService.findAll();

        //for every doctor get the appointments that are in between the start and end date (the appointment has the appointmentDateTime field)
        //then create a DoctorsThatHaveAppointmentsInPeriod object with the doctor's name, doctor's id and the start and end date.

        //we create the list of DoctorsThatHaveAppointmentsInPeriod objects
        List<DoctorsThatHaveAppointmentsInPeriod> doctorsThatHaveAppointmentsInPeriodList = new ArrayList<>();

        for (Doctor doctor : doctors) {
            List<Appointment> appointments = appointmentService.findAllByDoctorId(doctor.getId());
            List<Appointment> appointmentsInPeriod = appointments.stream()
                    .filter(appointment -> appointment.getAppointmentDateTime().isAfter(startDate) && appointment.getAppointmentDateTime().isBefore(endDate))
                    .toList();

            //if appointmentsInPeriod is empty we continue to the next doctor
            if (appointmentsInPeriod.isEmpty()) {
                continue;
            }

            //if its not empty we add the doctors data to the return dto object in the list
            DoctorsThatHaveAppointmentsInPeriod doctorsThatHaveAppointmentsInPeriod = new DoctorsThatHaveAppointmentsInPeriod();
            doctorsThatHaveAppointmentsInPeriod.setDoctorName(doctor.getName());
            doctorsThatHaveAppointmentsInPeriod.setDoctorId(doctor.getId());
            doctorsThatHaveAppointmentsInPeriod.setStartDate(startDate.toLocalDate());
            doctorsThatHaveAppointmentsInPeriod.setEndDate(endDate.toLocalDate());

            doctorsThatHaveAppointmentsInPeriodList.add(doctorsThatHaveAppointmentsInPeriod);
        }


        return doctorsThatHaveAppointmentsInPeriodList;
    }

    public MostSickLeavesMonthData getMostSickLeavesMonthData() {
        // Get all sick leaves
        List<SickLeave> sickLeaves = sickLeaveService.findAllSickLeaves();

        // Get the current year
        int currentYear = LocalDateTime.now().getYear();

        // Find the month with the most sick leaves for the current year
        Map<Integer, Long> sickLeavesCountByMonth = sickLeaves.stream()
                .filter(sickLeave -> sickLeave.getStartDate().getYear() == currentYear)
                .collect(Collectors.groupingBy(sickLeave -> sickLeave.getStartDate().getMonthValue(), Collectors.counting()));

        int mostSickLeavesMonth = sickLeavesCountByMonth.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);

        long mostSickLeavesCount = sickLeavesCountByMonth.getOrDefault(mostSickLeavesMonth, 0L);

        // Get the month name
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

        // Get all appointments for the month with the most sick leaves
        List<Appointment> allAppointmentsForMostSickLeavesMonth = appointmentService.findAll().stream()
                .filter(appointment -> appointment.getAppointmentDateTime().getMonthValue() == mostSickLeavesMonth && appointment.getAppointmentDateTime().getYear() == currentYear)
                .toList();

        // Get diagnosis statements for the month with the most sick leaves
        List<String> diagnosisStatements = allAppointmentsForMostSickLeavesMonth.stream()
                .flatMap(appointment -> appointment.getDiagnoses().stream())
                .map(Diagnosis::getStatement)
                .toList();

        // Find the most common diagnosis statement
        String mostCommonDiagnosis = diagnosisStatements.stream()
                .collect(Collectors.groupingBy(statement -> statement, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // Count unique patients for the month with the most sick leaves
        long uniquePatientsCount = allAppointmentsForMostSickLeavesMonth.stream()
                .map(Appointment::getPatient)
                .distinct()
                .count();

        // Create and return the MostSickLeavesMonthData object
        MostSickLeavesMonthData mostSickLeavesMonthData = new MostSickLeavesMonthData();
        mostSickLeavesMonthData.setMonthName(monthName);
        mostSickLeavesMonthData.setSickLeavesCount((int) mostSickLeavesCount);
        mostSickLeavesMonthData.setAppointmentsThatMonthCount(allAppointmentsForMostSickLeavesMonth.size());
        mostSickLeavesMonthData.setUniquePatientsCount((int) uniquePatientsCount);
        mostSickLeavesMonthData.setMostCommonDiagnosisThatMonth(mostCommonDiagnosis);

        return mostSickLeavesMonthData;
    }

    public List<DoctorsSickLeavesLeaderboardDto> getDoctorsSickLeavesLeaderboard() {
        // Get all doctors
        List<Doctor> doctors = doctorService.findAll();
        List<DoctorsSickLeavesLeaderboardDto> leaderboard = new ArrayList<>();

        // Get all appointments
        List<Appointment> appointments = appointmentService.findAll();

        // For each doctor, count the number of sick leaves
        for (Doctor doctor : doctors) {
            long sickLeavesCount = appointments.stream()
                    .filter(appointment -> appointment.getDoctor().getId().equals(doctor.getId()))
                    .flatMap(appointment -> appointment.getSickLeaves().stream())
                    .count();

            // Create a DTO and set the values
            DoctorsSickLeavesLeaderboardDto dto = new DoctorsSickLeavesLeaderboardDto();
            dto.setName(doctor.getName());
            dto.setSpecialties(doctor.getSpecialties());
            dto.setPrimaryCare(doctor.isPrimaryCare());
            dto.setSickLeavesCount((int) sickLeavesCount);
            leaderboard.add(dto);
        }

        // Sort the leaderboard by sick leaves count in descending order
        leaderboard.sort(Comparator.comparingInt(DoctorsSickLeavesLeaderboardDto::getSickLeavesCount).reversed());

        return leaderboard;
    }
}

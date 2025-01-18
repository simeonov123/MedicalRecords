package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.*;
import bg.nbu.medicalrecords.dto.*;
import bg.nbu.medicalrecords.util.MappingUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class StatisticsService {

    public final DiagnosisService diagnosisService;

    public final PatientService patientService;

    public final UserService userService;
    public final DoctorService doctorService;

    public final AppointmentService appointmentService;
    public StatisticsService(DiagnosisService diagnosisService, PatientService patientService, UserService userService, DoctorService doctorService, AppointmentService appointmentService) {
        this.diagnosisService = diagnosisService;
        this.patientService = patientService;
        this.userService = userService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
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
}

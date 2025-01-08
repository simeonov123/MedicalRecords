//package bg.nbu.medicalrecords.service;
//
//import bg.nbu.medicalrecords.domain.*;
//import bg.nbu.medicalrecords.dto.PatientDto;
//import bg.nbu.medicalrecords.exception.ResourceNotFoundException;
//import bg.nbu.medicalrecords.repository.*;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.time.Month;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class ReportService {
//    private final PatientRepository patientRepository;
//    private final DiagnosisRepository diagnosisRepository;
//    private final DoctorRepository doctorRepository;
//    private final AppointmentRepository appointmentRepository;
//    private final SickLeaveRepository sickLeaveRepository;
//    private UserService userService;
//
//    public ReportService(PatientRepository patientRepository,
//                         DiagnosisRepository diagnosisRepository,
//                         DoctorRepository doctorRepository,
//                         AppointmentRepository appointmentRepository,
//                         SickLeaveRepository sickLeaveRepository) {
//        this.patientRepository = patientRepository;
//        this.diagnosisRepository = diagnosisRepository;
//        this.doctorRepository = doctorRepository;
//        this.appointmentRepository = appointmentRepository;
//        this.sickLeaveRepository = sickLeaveRepository;
//    }
//
//    public List<PatientDto> findPatientsByDiagnosis(Long diagnosisId) {
//
//        return null;
//    }
//
//    public List<String> findMostCommonDiagnoses() {
//        return null;
//
//    }
//
//    public List<PatientDto> findPatientsByPrimaryDoctor(Long doctorId) {
//        Doctor doc = doctorRepository.findById(doctorId)
//                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
//        List<Patient> patients = patientRepository.findAll().stream()
//                .filter(p -> p.getPrimaryDoctor() != null && p.getPrimaryDoctor().equals(doc))
//                .collect(Collectors.toList());
//        return patients.stream().map(this::mapPatientToDto).collect(Collectors.toList());
//    }
//
//    public Map<Long, Long> countPatientsPerPrimaryDoctor() {
//        List<Patient> patients = patientRepository.findAll();
//        return patients.stream()
//                .filter(p -> p.getPrimaryDoctor() != null)
//                .collect(Collectors.groupingBy(p -> p.getPrimaryDoctor().getId(), Collectors.counting()));
//    }
//
//    public Map<Long, Long> countVisitsPerDoctor() {
//        List<Appointment> apps = appointmentRepository.findAll();
//        return apps.stream()
//                .collect(Collectors.groupingBy(a -> a.getDoctor().getId(), Collectors.counting()));
//    }
//
//    public Map<Long, Long> countVisitsPerPatient() {
//        List<Appointment> apps = appointmentRepository.findAll();
//        return apps.stream()
//                .collect(Collectors.groupingBy(a -> a.getPatient().getId(), Collectors.counting()));
//    }
//
//    public List<Appointment> findVisitsInPeriod(LocalDate start, LocalDate end) {
//        return appointmentRepository.findByDateBetween(start, end);
//    }
//
//    public List<Appointment> findVisitsForDoctorInPeriod(Long doctorId, LocalDate start, LocalDate end) {
//        return appointmentRepository.findByDoctorIdAndDateBetween(doctorId, start, end);
//    }
//
//    public Month findMonthWithMostSickLeaves() {
//        // Get all sick leaves and group by month of startDate for example
//        // or maybe consider startDate or issuance date if you have it. We have start_date/end_date.
//        // Assume start_date is issuance.
//        return sickLeaveRepository.findAll().stream()
//                .collect(Collectors.groupingBy(sl -> sl.getStartDate().getMonth(), Collectors.counting()))
//                .entrySet().stream().max(Map.Entry.comparingByValue())
//                .map(Map.Entry::getKey).orElse(null);
//    }
//
//    public List<Long> findDoctorsWhoIssuedMostSickLeaves() {
//        return null;
//
//    }
//
//    private PatientDto mapPatientToDto(Patient p) {
//        User user = userService.findByKeycloakUserId(p.getKeycloakUserId());
//
//        PatientDto dto = new PatientDto();
//        dto.setId(p.getId());
//        dto.setName(p.getName());
//        dto.setEgn(user.getEgn());
//        dto.setHealthInsurancePaid(p.isHealthInsurancePaid());
//        dto.setPrimaryDoctorId(p.getPrimaryDoctor() != null ? p.getPrimaryDoctor().getId() : null);
//        return dto;
//    }
//}

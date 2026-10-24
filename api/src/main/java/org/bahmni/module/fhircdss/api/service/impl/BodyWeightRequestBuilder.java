package org.bahmni.module.fhircdss.api.service.impl;

import org.apache.log4j.Logger;
import org.bahmni.module.fhircdss.api.service.RequestBuilder;
import org.bahmni.module.fhircdss.api.util.CdssUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * NMHS CDSS-HDA-004: latest recorded body weight (CIEL 5089 "Weight (kg)") for weight-based dose checks.
 * Looked up on the server so every client benefits; returns null (fail soft) when there is no usable weight.
 * Recency rules are applied by the CDS service, which also knows the patient's age.
 */
@Component
public class BodyWeightRequestBuilder implements RequestBuilder<Observation> {

    public static final String WEIGHT_CONCEPT_UUID = "5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String LOINC_SYSTEM = "http://loinc.org";
    public static final String BODY_WEIGHT_LOINC = "29463-7";
    public static final String UCUM_SYSTEM = "http://unitsofmeasure.org";

    private static final Logger logger = Logger.getLogger(BodyWeightRequestBuilder.class);

    private final PatientService patientService;
    private final ConceptService conceptService;
    private final ObsService obsService;

    @Autowired
    public BodyWeightRequestBuilder(PatientService patientService, ConceptService conceptService, ObsService obsService) {
        this.patientService = patientService;
        this.conceptService = conceptService;
        this.obsService = obsService;
    }

    @Override
    public Observation build(Bundle inputBundle) {
        try {
            String patientUuid = CdssUtils.getPatientUuidFromRequest(inputBundle);
            Patient patient = patientService.getPatientByUuid(patientUuid);
            Concept weightConcept = conceptService.getConceptByUuid(WEIGHT_CONCEPT_UUID);
            if (patient == null || weightConcept == null) {
                return null;
            }
            List<Obs> weights = obsService.getObservationsByPersonAndConcept(patient, weightConcept);
            if (weights == null) {
                return null;
            }
            Obs latest = weights.stream()
                    .filter(o -> !Boolean.TRUE.equals(o.getVoided()) && o.getValueNumeric() != null
                            && o.getValueNumeric() > 0 && o.getObsDatetime() != null)
                    .max(Comparator.comparing(Obs::getObsDatetime))
                    .orElse(null);
            if (latest == null) {
                return null;
            }
            Observation observation = new Observation();
            observation.setId(latest.getUuid());
            observation.setStatus(Observation.ObservationStatus.FINAL);
            observation.setCode(new CodeableConcept().addCoding(new Coding(LOINC_SYSTEM, BODY_WEIGHT_LOINC, "Body weight")));
            observation.setSubject(new Reference("Patient/" + patientUuid));
            observation.setEffective(new DateTimeType(latest.getObsDatetime()));
            observation.setValue(new Quantity().setValue(latest.getValueNumeric()).setUnit("kg").setSystem(UCUM_SYSTEM).setCode("kg"));
            return observation;
        } catch (RuntimeException e) {
            logger.warn("Could not read body weight for CDSS dose checks: " + e.getMessage());
            return null;
        }
    }
}

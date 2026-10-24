package org.bahmni.module.fhircdss.api.service.impl;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BodyWeightRequestBuilderTest {

    private static final String PATIENT_UUID = "37ce2350-760b-4f54-9501-1571707fd931";
    private PatientService patientService;
    private ConceptService conceptService;
    private ObsService obsService;
    private BodyWeightRequestBuilder builder;
    private Patient patient;
    private Concept weight;

    @Before
    public void setUp() {
        patientService = mock(PatientService.class);
        conceptService = mock(ConceptService.class);
        obsService = mock(ObsService.class);
        builder = new BodyWeightRequestBuilder(patientService, conceptService, obsService);
        patient = new Patient();
        weight = new Concept();
        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
        when(conceptService.getConceptByUuid(BodyWeightRequestBuilder.WEIGHT_CONCEPT_UUID)).thenReturn(weight);
    }

    private Bundle requestBundle() {
        org.hl7.fhir.r4.model.Patient fhirPatient = new org.hl7.fhir.r4.model.Patient();
        fhirPatient.setId(PATIENT_UUID);
        Bundle bundle = new Bundle();
        bundle.addEntry().setResource(fhirPatient);
        org.hl7.fhir.r4.model.MedicationRequest mr = new org.hl7.fhir.r4.model.MedicationRequest();
        mr.setSubject(new Reference("Patient/" + PATIENT_UUID));
        bundle.addEntry().setResource(mr);
        return bundle;
    }

    private Obs obs(double kg, long time, boolean voided) {
        Obs o = new Obs();
        o.setValueNumeric(kg);
        o.setObsDatetime(new Date(time));
        o.setVoided(voided);
        o.setUuid("obs-" + kg);
        return o;
    }

    @Test
    public void shouldReturnLatestNonVoidedWeight() {
        when(obsService.getObservationsByPersonAndConcept(any(), any())).thenReturn(Arrays.asList(
                obs(12.0, 1_000L, false), obs(14.5, 3_000L, false), obs(99.0, 5_000L, true)));
        Observation observation = builder.build(requestBundle());
        assertEquals(14.5, ((Quantity) observation.getValue()).getValue().doubleValue(), 0.0001);
        assertEquals("kg", ((Quantity) observation.getValue()).getCode());
        assertEquals("29463-7", observation.getCode().getCodingFirstRep().getCode());
        assertEquals(3_000L, observation.getEffectiveDateTimeType().getValue().getTime());
    }

    @Test
    public void shouldReturnNullWhenNoWeightRecorded() {
        when(obsService.getObservationsByPersonAndConcept(any(), any())).thenReturn(Collections.emptyList());
        assertNull(builder.build(requestBundle()));
    }

    @Test
    public void shouldFailSoftWhenLookupThrows() {
        when(obsService.getObservationsByPersonAndConcept(any(), any())).thenThrow(new RuntimeException("db down"));
        assertNull(builder.build(requestBundle()));
    }
}

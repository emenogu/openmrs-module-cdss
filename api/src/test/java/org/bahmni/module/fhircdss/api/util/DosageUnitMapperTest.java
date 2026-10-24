package org.bahmni.module.fhircdss.api.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DosageUnitMapperTest {

    @Test
    public void shouldPreserveMilligramDoseUnitForCdssDosageValidation() {
        assertEquals("mg", DosageUnitMapper.getTargetUnit("mg"));
    }

    // NMHS CDSS-HDA-002: Bahmni standard-config dosing unit names (dosingUnits.csv)
    @Test
    public void shouldMapBahmniStandardDosingUnitNames() {
        assertEquals("mg", DosageUnitMapper.getTargetUnit("Milligram", null, null));
        assertEquals("mL", DosageUnitMapper.getTargetUnit("Milliliter", null, null));
        assertEquals("Actuation", DosageUnitMapper.getTargetUnit("Puff", null, null));
        assertEquals("Tablet", DosageUnitMapper.getTargetUnit("Tablet", null, null));
    }

    @Test
    public void shouldMapUnitNamesCaseInsensitively() {
        assertEquals("mg", DosageUnitMapper.getTargetUnit("milligram", null, null));
        assertEquals("mL", DosageUnitMapper.getTargetUnit("ML", null, null));
    }

    @Test
    public void shouldFallBackToUcumCodeWhenNameIsUnknown() {
        assertEquals("mg", DosageUnitMapper.getTargetUnit("milligrams per dose", DosageUnitMapper.UCUM_SYSTEM, "mg"));
        assertEquals("mcg", DosageUnitMapper.getTargetUnit(null, DosageUnitMapper.UCUM_SYSTEM, "ug"));
        assertNull(DosageUnitMapper.getTargetUnit("milligrams per dose", "http://example.org", "mg"));
    }

    @Test
    public void shouldKeepUnsupportedUnitsUnmapped() {
        assertNull(DosageUnitMapper.getTargetUnit("International units", null, null));
        assertNull(DosageUnitMapper.getTargetUnit("Unit", null, null));
    }

    @Test
    public void shouldMapPerRectumRoute() {
        assertEquals("R", DosageRouteMapper.getTargetRoute("Per Rectum"));
        assertEquals("R", DosageRouteMapper.getTargetRoute("Rectum"));
    }
}

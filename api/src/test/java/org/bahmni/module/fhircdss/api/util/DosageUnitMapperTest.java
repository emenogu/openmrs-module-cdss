package org.bahmni.module.fhircdss.api.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DosageUnitMapperTest {

    @Test
    public void shouldPreserveMilligramDoseUnitForCdssDosageValidation() {
        assertEquals("mg", DosageUnitMapper.getTargetUnit("mg"));
    }
}

package org.bahmni.module.fhircdss.api.util;

import java.util.HashMap;
import java.util.Map;

public enum DosageUnitMapper {

    L_L("l", "L"),
    ML_ML("ml", "mL"),
    MG_MG("mg", "mg"),
    UL_UL("ul", "uL"),
    TABLETS_TABLET("Tablet(s)", "Tablet"),
    TABLET_TABLET("Tablet", "Tablet"),
    CAPSULES_CAPSULE("Capsule(s)", "Capsule"),
    CAPSULE_CAPSULE("Capsule", "Capsule"),
    PUFF_PUFF("Puff(s)", "Actuation"),
    DROP_DROP("Drop", "Drop"),
    TABLESPOON_SPOONFUL("Tablespoon", "Spoonful"),
    TEASPOON("Teaspoon", "Spoonful"),
    UNIT_CAPSULE("Unit(s)", "Capsule"),
    // NMHS CDSS-HDA-002: Bahmni standard-config dosing unit names (dosingUnits.csv) and mass units.
    MILLILITER_ML("Milliliter", "mL"),
    MILLILITRE_ML("Millilitre", "mL"),
    MILLIGRAM_MG("Milligram", "mg"),
    GRAM_G("Gram", "g"),
    MICROGRAM_MCG("Microgram", "mcg"),
    MCG_MCG("mcg", "mcg"),
    G_G("g", "g"),
    PUFF_ACTUATION("Puff", "Actuation");

    private static final Map<String, String> BY_UNIT = new HashMap<>();
    private static final Map<String, String> BY_UNIT_IGNORE_CASE = new HashMap<>();
    /** UCUM codes (FHIR Quantity.system http://unitsofmeasure.org) understood by the CDS service. */
    private static final Map<String, String> BY_UCUM = new HashMap<>();
    public static final String UCUM_SYSTEM = "http://unitsofmeasure.org";

    static {
        for (DosageUnitMapper e : values()) {
            BY_UNIT.put(e.inputUnit, e.getTargetUnit());
            BY_UNIT_IGNORE_CASE.putIfAbsent(e.inputUnit.trim().toLowerCase(), e.getTargetUnit());
        }
        BY_UCUM.put("mg", "mg");
        BY_UCUM.put("g", "g");
        BY_UCUM.put("ug", "mcg");
        BY_UCUM.put("mcg", "mcg");
        BY_UCUM.put("mL", "mL");
        BY_UCUM.put("L", "L");
        BY_UCUM.put("uL", "uL");
    }

    /** Unit name first (exact, then case-insensitive), then the UCUM code when the quantity carries one. */
    public static String getTargetUnit(String sourceUnit, String system, String code) {
        String target = getTargetUnit(sourceUnit);
        if (target == null && sourceUnit != null) {
            target = BY_UNIT_IGNORE_CASE.get(sourceUnit.trim().toLowerCase());
        }
        if (target == null && UCUM_SYSTEM.equals(system) && code != null) {
            target = BY_UCUM.get(code);
        }
        return target;
    }

    public final String inputUnit;
    public final String targetUnit;

    private DosageUnitMapper(String inputUnit, String targetUnit) {
        this.inputUnit = inputUnit;
        this.targetUnit = targetUnit;
    }

    public static String getTargetUnit(String sourceUnit) {
        return BY_UNIT.get(sourceUnit);
    }

    //getter
    public String getInputUnit() {
        return inputUnit;
    }


    public String getTargetUnit() {
        return targetUnit;
    }
}

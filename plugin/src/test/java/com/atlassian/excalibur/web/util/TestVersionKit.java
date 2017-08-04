package com.atlassian.excalibur.web.util;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestVersionKit {
    @Test
    public void testParsingAndComparison() throws Exception {
        VersionKit.SoftwareVersion oneSevenTwo = VersionKit.parse("1.7.2");
        assertIsVersion(oneSevenTwo, 1, 7, 2);

        VersionKit.SoftwareVersion oneSevenThree = VersionKit.parse("1.7.3");
        assertIsVersion(oneSevenThree, 1, 7, 3);

        VersionKit.SoftwareVersion oneSevenTwoFour = VersionKit.parse("1.7.2.4");
        assertIsVersion(oneSevenTwoFour, 1, 7, 2);

        VersionKit.SoftwareVersion oneSevenTwoSnapshot = VersionKit.parse("1.7.2-SNAPSHOT");
        assertIsVersion(oneSevenTwoSnapshot, 1, 7, 2);

        VersionKit.SoftwareVersion oneSevenSnapshot = VersionKit.parse("1.7-SNAPSHOT");
        assertIsVersion(oneSevenSnapshot, 1, 7, 0);

        VersionKit.SoftwareVersion oneSeven = VersionKit.parse("1.7");
        assertIsVersion(oneSeven, 1, 7, 0);

        VersionKit.SoftwareVersion twoNineThree = VersionKit.parse("2.9.3");
        assertIsVersion(twoNineThree, 2, 9, 3);

        VersionKit.SoftwareVersion oneSixNine = VersionKit.parse("1.6.9");

        assertBadParse("XXXX.1.6.9");
        assertBadParse("1.X.9");

        // 1.7.2 > 1.7
        assertTrue(oneSevenTwo.isGreaterThanOrEqualTo(oneSeven));
        assertTrue(oneSevenTwo.isGreaterThanOrEqualTo(oneSevenTwo));
        assertFalse(oneSeven.isGreaterThanOrEqualTo(oneSevenTwo));

        assertTrue(oneSevenTwo.isGreaterThan(oneSeven));
        assertFalse(oneSeven.isGreaterThan(oneSevenTwo));
        assertFalse(oneSeven.isGreaterThan(oneSeven));

        assertTrue(oneSeven.isLessThanOrEqualTo(oneSevenTwo));
        assertTrue(oneSeven.isLessThanOrEqualTo(oneSeven));
        assertFalse(oneSevenTwo.isLessThanOrEqualTo(oneSeven));

        assertTrue(oneSeven.isLessThan(oneSevenTwo));
        assertFalse(oneSevenTwo.isLessThan(oneSeven));
        assertFalse(oneSevenTwo.isLessThan(oneSevenTwo));


        // 1.7.3 > 1.7.2
        assertTrue(oneSevenThree.isGreaterThanOrEqualTo(oneSevenTwo));
        assertTrue(oneSevenThree.isGreaterThanOrEqualTo(oneSevenThree));
        assertFalse(oneSevenTwo.isGreaterThanOrEqualTo(oneSevenThree));

        assertTrue(oneSevenThree.isGreaterThan(oneSevenTwo));
        assertFalse(oneSevenThree.isGreaterThan(oneSevenThree));
        assertFalse(oneSevenTwo.isGreaterThan(oneSevenThree));

        assertTrue(oneSevenTwo.isLessThanOrEqualTo(oneSevenThree));
        assertTrue(oneSevenTwo.isLessThanOrEqualTo(oneSevenTwo));
        assertFalse(oneSevenThree.isLessThanOrEqualTo(oneSevenTwo));

        assertTrue(oneSevenTwo.isLessThan(oneSevenThree));
        assertTrue(oneSevenTwo.isLessThan(twoNineThree));
        assertFalse(oneSevenThree.isLessThan(oneSevenTwo));
        assertFalse(oneSevenTwo.isLessThan(oneSevenTwo));

        assertTrue(oneSevenThree.isGreaterThan(oneSevenTwo));
        assertFalse(oneSevenTwo.isGreaterThan(oneSevenThree));
        assertFalse(oneSevenThree.isGreaterThan(oneSevenThree));


        // 2.9.3 --> 1.7.3
        assertTrue(twoNineThree.isGreaterThanOrEqualTo(oneSevenThree));
        assertTrue(twoNineThree.isGreaterThan(oneSevenTwo));
        assertTrue(oneSevenTwo.isLessThanOrEqualTo(twoNineThree));
        assertTrue(oneSevenTwo.isLessThan(twoNineThree));

        assertTrue(oneSevenTwo.isGreaterThanOrEqualTo(oneSixNine));
        assertFalse(oneSixNine.isGreaterThanOrEqualTo(oneSevenTwo));
        assertFalse(oneSixNine.isGreaterThanOrEqualTo(oneSeven));
        assertFalse(oneSixNine.isGreaterThanOrEqualTo(oneSeven));

        VersionKit.SoftwareVersion ninetyNineNineNine = VersionKit.parse("99.9.9");
        VersionKit.SoftwareVersion elevenHundredOneOne = VersionKit.parse("111.1.1");

        assertTrue(elevenHundredOneOne.isGreaterThanOrEqualTo(ninetyNineNineNine));
        assertFalse(ninetyNineNineNine.isGreaterThanOrEqualTo(elevenHundredOneOne));

    }

    @Test
    public void testVer() throws Exception {
        assertIsVersion(VersionKit.version(99, 9, 9), 99, 9, 9);
        assertIsVersion(VersionKit.version(99, 9), 99, 9, 0);
        assertIsVersion(VersionKit.version(99), 99, 0, 0);

        assertIsVersion(VersionKit.version(99, 9, 9, 1, 2, 3, 4), 99, 9, 9);

    }

    private void assertBadParse(final String dottedVersionString) {
        try {
            VersionKit.parse(dottedVersionString);
            fail("cause that is a rubbish version");
        } catch (IllegalArgumentException expected) {
        }
    }

    private void assertIsVersion(VersionKit.SoftwareVersion oneSevenTwo, final int expectedMajor, final int expectedMinor, final int expectedBugFix) {
        Assert.assertEquals(expectedMajor, oneSevenTwo.getMajorVersion());
        Assert.assertEquals(expectedMinor, oneSevenTwo.getMinorVersion());
        Assert.assertEquals(expectedBugFix, oneSevenTwo.getBugFixVersion());
    }
}

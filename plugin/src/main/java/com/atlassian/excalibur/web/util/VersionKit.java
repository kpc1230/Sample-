package com.atlassian.excalibur.web.util;

import com.atlassian.jira.util.BuildUtilsInfo;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helps with versions comparisons
 */
public class VersionKit {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.?(\\d+)?");

    public static class SoftwareVersion {
        private final int majorVersion;
        private final int minorVersion;
        private final int bugFixVersion;
        private final String dottedVersionString;

        public SoftwareVersion(final String dottedVersionString) {
            this.dottedVersionString = dottedVersionString;
            Matcher versionMatcher = VERSION_PATTERN.matcher(dottedVersionString);
            if (versionMatcher.find()) {

                majorVersion = decode(versionMatcher, 1, 0);
                minorVersion = decode(versionMatcher, 2, 0);
                bugFixVersion = decode(versionMatcher, 3, 0);
            } else {
                throw new IllegalArgumentException("The dotted version string is not in the expected format");
            }
        }

        public SoftwareVersion(final int majorVersion, final int minorVersion, final int bugfixVersion) {
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.bugFixVersion = bugfixVersion;
            this.dottedVersionString = "" + majorVersion + "." + minorVersion + "." + bugfixVersion;
        }

        public SoftwareVersion(final int majorVersion, final int minorVersion) {
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.bugFixVersion = 0;
            this.dottedVersionString = "" + majorVersion + "." + minorVersion;
        }

        private int decode(Matcher versionMatcher, int i, int defaultVal) {
            if (versionMatcher.group(i) != null) {
                return Integer.decode(versionMatcher.group(i));
            }
            return defaultVal;
        }

        public int getMajorVersion() {
            return majorVersion;
        }

        public int getMinorVersion() {
            return minorVersion;
        }

        public int getBugFixVersion() {
            return bugFixVersion;
        }

        /**
         * Returns true if this version is greater than if equal to the specified version
         *
         * @param that the specified version to compare against
         * @return true if this version is greater than if equal to the specified version
         */
        public boolean isGreaterThanOrEqualTo(SoftwareVersion that) {
            if (this.equals(that)) {
                return true;
            }
            if (this.majorVersion < that.majorVersion) {
                return false;
            }
            if (this.majorVersion == that.majorVersion) {
                if (this.minorVersion < that.minorVersion) {
                    return false;
                }
                if (this.minorVersion == that.minorVersion) {
                    if (this.bugFixVersion < that.bugFixVersion) {
                        return false;
                    }
                }
            }
            return true;
        }

        /**
         * Returns true if this version is less than or equal to the specified version
         *
         * @param that the specified version to compare against
         * @return true if this version is less than or equal to the specified version
         */
        public boolean isLessThanOrEqualTo(SoftwareVersion that) {
            if (this.equals(that)) {
                return true;
            }
            if (this.majorVersion > that.majorVersion) {
                return false;
            }
            if (this.majorVersion == that.majorVersion) {
                if (this.minorVersion > that.minorVersion) {
                    return false;
                }
                if (this.minorVersion == that.minorVersion) {
                    if (this.bugFixVersion > that.bugFixVersion) {
                        return false;
                    }
                }
            }
            return true;
        }

        /**
         * Returns true if this version is greater than the specified version
         *
         * @param that the specified version to compare against
         * @return true if this version is greater than to the specified version
         */
        public boolean isGreaterThan(SoftwareVersion that) {
            if (this.majorVersion > that.majorVersion) {
                return true;
            }
            if (this.majorVersion == that.majorVersion) {
                if (this.minorVersion > that.minorVersion) {
                    return true;
                }
                if (this.minorVersion == that.minorVersion) {
                    if (this.bugFixVersion > that.bugFixVersion) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Returns true if this version is less than the specified version
         *
         * @param that the specified version to compare against
         * @return true if this version is less than to the specified version
         */
        public boolean isLessThan(SoftwareVersion that) {
            if (this.majorVersion < that.majorVersion) {
                return true;
            }
            if (this.majorVersion == that.majorVersion) {
                if (this.minorVersion < that.minorVersion) {
                    return true;
                }
                if (this.minorVersion == that.minorVersion) {
                    if (this.bugFixVersion < that.bugFixVersion) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            SoftwareVersion that = (SoftwareVersion) o;

            if (bugFixVersion != that.bugFixVersion) {
                return false;
            }
            if (majorVersion != that.majorVersion) {
                return false;
            }
            if (minorVersion != that.minorVersion) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = majorVersion;
            result = 31 * result + minorVersion;
            result = 31 * result + bugFixVersion;
            return result;
        }

        @Override
        public String toString() {
            return dottedVersionString;
        }
    }


    /**
     * Parses and returns a SoftwareVersion object representing the dotted number string.
     *
     * @param dottedVersionString the input version
     * @return a version domain object
     * @throws IllegalArgumentException if the string is not N.N.N
     */
    public static SoftwareVersion parse(final String dottedVersionString) {
        return new SoftwareVersion(dottedVersionString);
    }

    public static SoftwareVersion version(final int majorVersion, final int... versions) {
        int minorVersion = readArray(versions, 0, 0);
        int bugFixVersion = readArray(versions, 1, 0);
        return new SoftwareVersion(majorVersion, minorVersion, bugFixVersion);
    }

    public static SoftwareVersion version(final BuildUtilsInfo buildUtilsInfo) {
        int[] versionNumbers = buildUtilsInfo.getVersionNumbers();
        return version(versionNumbers[0], Arrays.copyOfRange(versionNumbers, 1, versionNumbers.length));
    }

    private static int readArray(int[] versions, int index, int defaultVal) {
        if (index >= versions.length) {
            return defaultVal;
        }
        return versions[index];
    }
}
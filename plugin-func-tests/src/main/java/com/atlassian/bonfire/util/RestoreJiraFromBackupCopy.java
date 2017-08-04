package com.atlassian.bonfire.util;

import com.atlassian.excalibur.web.util.ReflectionKit;
import com.atlassian.integrationtesting.runner.CompositeTestRunner;
import com.atlassian.integrationtesting.runner.CompositeTestRunner.AfterTestMethod;
import com.atlassian.integrationtesting.runner.CompositeTestRunner.BeforeTestClass;
import com.atlassian.integrationtesting.runner.CompositeTestRunner.BeforeTestMethod;
import com.atlassian.integrationtesting.runner.CompositeTestRunner.Composer;
import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.rule.data.SqlDataImporter;
import com.atlassian.jira.functest.rule.tenant.TestDatabase;
import com.atlassian.jira.functest.rule.tenant.TestTenantContext;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.config.RestoreJiraData;
import com.google.common.base.Function;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * This was copied from JIRA because it was removed in 5.2 This will eventually be updated into it's own standalone thing, but until then We will need
 * a copy here
 *
 * @author ezhang
 */

public class RestoreJiraFromBackupCopy {

    private static CaptureTenantDatabaseProvisioner databaseProvisioner;

    public static final String LICENSE = "AAACwA0ODAoPeNqVVV1v2jAUfc+vsLSXrVLSJEN0rRRpQNIVjQIKSbd124PjXMAlsZE/SvvvZ0Loo" +
            "E2YFiFFju17zzn33sO7ZKlRCAT5PvLcK69z5XooTQbId72u9UAFdtaC55ooZ7uwCRfg9LTiMTDYB" +
            "EposGYgHkEMw6D/5TKxv6d3Hfvr/f2N3Xe9b9aIEmASkuc1jHEJQRjdRaPJNIqtWTQOEpDKvC3C2" +
            "dzBRNFHCOa4kLC/Fz2tqXgOsYJAs4KWVEHeBqu+X2Gqjhx+yEwGag4lguIiYnkV0ncvL23XM7+Gm" +
            "JLP1QabK31aFJQtpiAozwPPt8a6zEBM5qkEIQPbs0KQRNC1opxVlFCxQ48UR0IzNMBrpQWgaaEXl" +
            "L1gacLHdHnDpWG5C+67rmvV0e7Mh20G34oecaFxlW6nVosib5Dvs7zZuMWUKWCYkUPJfbfbtT3f9" +
            "jqn9GmCPBBQ4avDePswIyzVLc/pnILJ2/nkdS8+dt3t08ahrWBnZ2eDOOolUWj3f5hFsFoIypa8I" +
            "J+xKrCUFDOH8NLiLIcSs/ygMZp5HJe1opHhMuP8ZadfLUec4KK3AKaq4tdn9g2b06ou0TiJ4mk8n" +
            "EWnUraRG3CmTHdEpixFK7FGcDGUXEGNzj+hawNbIvgmP+pKeFIgGC5STfNgpnSG3l/jFXxAaToMd" +
            "5H/i7dxCnM8B7k6nvYTEjUNyYGpHBjKfveVPZneGE8S+3oS29N4EqaDZDgZ2+ks2jZN1aeQo+wZq" +
            "SWgOjCKGOE5CGQgPQBR6OdSqfWvq/PzBXeOinBez6YNuxu/HRRyxLhCOZVK0Ewr4wJLKrdWQLRUv" +
            "DRyO821mxaYVS0109lfQ6lITMQCMyp3M9/bIzil2ysVambGpVO2YnzD2vqiRdsd3n8UrQrQZAcnc" +
            "Dbnm2pBlljCa/+oB6P6K2mbiz8QiWcZMCwCFCaADpRX5kbRrQdChFoZ6BZRw1lRAhQSNzvwOvomj" +
            "hGYCc6Dhc2ohg7wuw==X0211g";

    public static Composer compose(JiraTestedProduct product) {
        databaseProvisioner = new CaptureTenantDatabaseProvisioner(new SqlDataImporter(), product.environmentData());
        CaptureTenantDatabaseProvisioner.setupVertigoTestTenant();
        return CompositeTestRunner.compose().beforeTestClass(new BeforeClass(product)).beforeTestMethod(new BeforeMethod(product))
                .afterTestMethod(new AfterMethod(product));
    }

    private static final class BeforeClass implements Function<BeforeTestClass, Void> {
        private final JiraTestedProduct product;
        private static final Set<Class<?>> alreadyRunClasses = Sets.newHashSet();

        public BeforeClass(JiraTestedProduct product) {
            this.product = product;
        }

        public Void apply(BeforeTestClass test) {
            if (test.hasAnnotation(RestoreOnce.class)) {
                if (test.hasAnnotation(Restore.class)) {
                    throw new RuntimeException("Both @Restore and @RestoreOnce found on class. Only one should be present.");
                }
                if (!alreadyRunClasses.contains(test.testClass.getJavaClass())) {
                    RestoreOnce restoreOnce = test.getAnnotation(RestoreOnce.class);
                    RestoreJiraData dataPO = product.injector().getInstance(RestoreJiraData.class);
                    try {
                        ReflectionKit.method(dataPO, "execute", String.class).call(restoreOnce.value());
                    } catch (RuntimeException e) {
                        //If the above fails in any way, try calling restore instead, otherwise you're on your own.
                        if (!product.environmentData().isVertigoMode()) {
                            product.backdoor().restoreDataFromResource(restoreOnce.value(), LICENSE);
                        } else {
                            String tenantId = TestTenantContext.getCloudTenantId().get();
                            databaseProvisioner.create(tenantId, restoreOnce.value().replace(".xml", ".sql"));
                        }
                    }
                    alreadyRunClasses.add(test.testClass.getJavaClass());
                }
            }
            return null;
        }
    }

    private static final class BeforeMethod implements Function<BeforeTestMethod, Void> {
        private final JiraTestedProduct product;

        public BeforeMethod(JiraTestedProduct product) {
            this.product = product;
        }

        public Void apply(BeforeTestMethod test) {
            if (test.hasAnnotation(Restore.class)) {
                Restore restore = test.getAnnotation(Restore.class);
                RestoreJiraData dataPO = product.injector().getInstance(RestoreJiraData.class);
                try {
                    ReflectionKit.method(dataPO, "execute", String.class).call(restore.value());
                } catch (Exception e) {
                    // If the above fails in any way, try calling restore instead, otherwise you're on your own.
                    if (!product.environmentData().isVertigoMode()) {
                        product.backdoor().restoreDataFromResource(restore.value(), LICENSE);
                    } else {
                        String tenantId = TestTenantContext.getCloudTenantId().get();
                        databaseProvisioner.create(tenantId, restore.value().replace(".xml", ".sql"));
                    }
                }
            }
            return null;
        }
    }

    private static final class AfterMethod implements Function<AfterTestMethod, Void> {
        private final JiraTestedProduct product;

        public AfterMethod(JiraTestedProduct product) {
            this.product = product;
        }

        public Void apply(AfterTestMethod test) {
            if (test.hasAnnotation(Restore.class) && test.hasAnnotation(RestoreOnce.class)) {
                RestoreJiraData dataPO = product.injector().getInstance(RestoreJiraData.class);
                String sourceData = test.getAnnotation(RestoreOnce.class).value();
                try {
                    ReflectionKit.method(dataPO, "execute", String.class).call(sourceData);
                } catch (Exception e) {
                    // If the above fails in any way, try calling restore instead, otherwise you're on your own.
                    if (!product.environmentData().isVertigoMode()) {
                        product.backdoor().restoreDataFromResource(sourceData, LICENSE);
                    } else {
                        String tenantId = TestTenantContext.getCloudTenantId().get();
                        databaseProvisioner.create(tenantId, sourceData.replace(".xml", ".sql"));
                    }
                }
            }
            return null;
        }
    }
}

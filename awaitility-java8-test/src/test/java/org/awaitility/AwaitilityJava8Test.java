/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.awaitility;

import org.assertj.core.api.Assertions;
import org.awaitility.classes.Asynch;
import org.awaitility.classes.FakeRepository;
import org.awaitility.classes.FakeRepositoryImpl;
import org.awaitility.core.ConditionEvaluationLogger;
import org.awaitility.core.ConditionTimeoutException;
import org.awaitility.core.ThrowingRunnable;
import org.awaitility.support.CountDown;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.with;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for await().until(Runnable) using AssertionCondition.
 *
 * @author Marcin Zajączkowski, 2014-03-28
 * @author Johan Haleby
 */
public class AwaitilityJava8Test {

    private FakeRepository fakeRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        fakeRepository = new FakeRepositoryImpl();
        Awaitility.reset();
    }

    @Test(timeout = 2000)
    public void awaitAssertJAssertionAsLambda() {
        new Asynch(fakeRepository).perform();
        await().untilAsserted(() -> Assertions.assertThat(fakeRepository.getValue()).isEqualTo(1));
    }

    @Test(timeout = 2000)
    public void awaitUsingLambdaVersionOfCallableBoolean() {
        new Asynch(fakeRepository).perform();
        await().until(() -> fakeRepository.getValue() == 1);
    }

    @SuppressWarnings("Convert2Lambda")
    @Test(timeout = 2000)
    public void awaitAssertJAssertionAsAnonymousClass() {
        new Asynch(fakeRepository).perform();
        await().untilAsserted(new ThrowingRunnable() {
            @Override
            public void run() {
                Assertions.assertThat(fakeRepository.getValue()).isEqualTo(1);
            }
        });
    }

    @Test(timeout = 2000)
    public void awaitAssertJAssertionDisplaysOriginalErrorMessageAndTimeoutWhenConditionTimeoutExceptionOccurs() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage(startsWith("Assertion condition defined as a lambda expression in " + AwaitilityJava8Test.class.getName()));
        exception.expectMessage(endsWith("expected:<[1]> but was:<[0]> within 120 milliseconds."));

        new Asynch(fakeRepository).perform();
        with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).untilAsserted(
                () -> Assertions.assertThat(fakeRepository.getValue()).isEqualTo(1));
    }

    @Test(timeout = 2000)
    public void awaitJUnitAssertionAsLambda() {
        new Asynch(fakeRepository).perform();
        await().untilAsserted(() -> assertEquals(1, fakeRepository.getValue()));
    }

    @Test(timeout = 2000)
    public void awaitJUnitAssertionDisplaysOriginalErrorMessageAndTimeoutWhenConditionTimeoutExceptionOccurs() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage(startsWith("Assertion condition defined as a lambda expression in " + AwaitilityJava8Test.class.getName()));
        exception.expectMessage(endsWith("expected:<1> but was:<0> within 120 milliseconds."));

        with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).untilAsserted(
                () -> assertEquals(1, fakeRepository.getValue()));
    }

    @Test(timeout = 2000)
    public void lambdaErrorMessageLooksAlrightWhenUsingMethodReferences() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("Lambda expression in org.awaitility.AwaitilityJava8Test that uses org.awaitility.classes.FakeRepository: expected <1> but was <0> within 120 milliseconds.");
        with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(fakeRepository::getValue, equalTo(1));
    }

    @SuppressWarnings("Convert2MethodRef")
    @Test(timeout = 2000)
    public void lambdaErrorMessageLooksAlrightWhenUsingLambda() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("Lambda expression in org.awaitility.AwaitilityJava8Test: expected <1> but was <0> within 120 milliseconds.");

        with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(() -> fakeRepository.getValue(), equalTo(1));
    }

    @SuppressWarnings({"Convert2MethodRef", "CodeBlock2Expr"})
    @Test(timeout = 2000)
    public void lambdaErrorMessageLooksAlrightWhenUsingLambdaWithCurlyBraces() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("Lambda expression in org.awaitility.AwaitilityJava8Test: expected <1> but was <0> within 120 milliseconds.");

        with().pollInterval(10, MILLISECONDS).then().await().atMost(120, MILLISECONDS).until(() -> {
            return fakeRepository.getValue();
        }, equalTo(1));
    }

    @Test(timeout = 2000)
    public void lambdaErrorMessageLooksAlrightWhenAwaitUsingLambdaVersionOfCallableBoolean() {
        exception.expect(ConditionTimeoutException.class);
        exception.expectMessage("Condition with lambda expression in org.awaitility.AwaitilityJava8Test was not fulfilled within 200 milliseconds.");

        await().atMost(200, MILLISECONDS).until(() -> fakeRepository.getValue() == 2);
    }

    @Test(timeout = 10000)
    public void conditionResultsCanBeLoggedToSystemOut() {
        with()
                .conditionEvaluationListener(condition -> System.out.printf("%s (elapsed time %dms, remaining time %dms)\n", condition.getDescription(), condition.getElapsedTimeInMS(), condition.getRemainingTimeInMS()))
                .pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .atMost(Duration.TWO_SECONDS)
                .until(new CountDown(5), anyOf(is(0), lessThan(0)));
    }

    @Test(timeout = 10000)
    public void loggingIntermediaryHandlerLogsToSystemOut() {
        with()
                .conditionEvaluationListener(new ConditionEvaluationLogger(SECONDS))
                .pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .atMost(Duration.TWO_SECONDS)
                .until(new CountDown(5), is(equalTo(0)));
    }

    @Test public void
    canMakeUseOfThrowingMethodInAwaitilityToWrapRunnablesThatThrowsExceptions() {
        await().untilAsserted(() -> stringEquals("test", "test"));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(timeout = 2000L)
    public void includesCauseInStackTrace()  {
        try {
            await().atMost(200, MILLISECONDS).untilAsserted(() -> {
                assertNotNull("34");
                assertNotNull(null);
            });
            fail("Should throw ConditionTimeoutException");
        } catch (ConditionTimeoutException e) {
            assertThat(e.getCause().getClass().getName()).isEqualTo(AssertionError.class.getName());
        }
    }

    // This was previously a bug (https://github.com/awaitility/awaitility/issues/78)
    @Test(timeout = 2000L)
    public void throwsExceptionImmediatelyWhenCallableConditionThrowsAssertionError() throws Exception {
        // Given
        long timeStart = System.currentTimeMillis();
        new Asynch(fakeRepository).perform();

        // When
        final AtomicInteger counter = new AtomicInteger(0);
        try {
            await().atMost(1500, MILLISECONDS).until(() -> {
                counter.incrementAndGet();
                assertTrue(counter.get() >= 2);
                return true;
            });
            fail("Expecting error");
        } catch (AssertionError ignored) {
            // expected
        }

        // Then
        long timeEnd = System.currentTimeMillis();
        assertThat(timeEnd - timeStart).isLessThan(1500L);
    }

    private void stringEquals(String first, String second) {
        Assertions.assertThat(first).isEqualTo(second);
    }
}

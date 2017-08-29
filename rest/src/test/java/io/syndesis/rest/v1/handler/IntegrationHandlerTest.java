package io.syndesis.rest.v1.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.Validator;

import io.syndesis.dao.manager.DataManager;
import io.syndesis.inspector.ClassInspector;
import io.syndesis.model.connection.Action;
import io.syndesis.model.connection.DataShape;
import io.syndesis.model.filter.FilterOptions;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.SimpleStep;
import io.syndesis.rest.v1.handler.integration.IntegrationHandler;
import org.junit.Before;
import org.junit.Test;

import static io.syndesis.model.connection.DataShapeKinds.JAVA;
import static io.syndesis.model.connection.DataShapeKinds.NONE;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author roland
 * @since 29.08.17
 */
public class IntegrationHandlerTest {


    private IntegrationHandler handler;
    private ClassInspector inspector;
    private Validator validator;
    private DataManager manager;

    @Before
    public void setup() {
        manager = mock(DataManager.class);
        validator = mock(Validator.class);
        inspector = mock(ClassInspector.class);
        handler = new IntegrationHandler(manager, validator, inspector);
    }

    @Test
    public void filterOptions_simple() {
        when(inspector.getPaths("twitter4j.Status")).thenReturn(Arrays.asList("paramA", "paramB"));
        Integration integration =
            createIntegrationFromDataShapes(dataShape(JAVA, "twitter4j.Status"),null,dataShape(NONE));

        FilterOptions options = handler.getFilterOptions(integration, -1);
        assertThat(options.getPaths()).hasSize(2).contains("paramA","paramB");
    }

    @Test
    public void filterOptions_multipleOutputShapes() {
        when(inspector.getPaths("twitter4j.Status")).thenReturn(Arrays.asList("paramA", "paramB"));
        when(inspector.getPaths("blubber.bla")).thenReturn(Arrays.asList("paramY", "paramZ"));
        Integration integration =
            createIntegrationFromDataShapes(dataShape(JAVA, "blubber.bla"),null, dataShape(JAVA, "twitter4j.Status"));

        assertThat(handler.getFilterOptions(integration, -1).getPaths())
            .hasSize(2).contains("paramA", "paramB");

        assertThat(handler.getFilterOptions(integration, 1).getPaths())
            .hasSize(2).contains("paramY","paramZ");
    }

    @Test
    public void filterOptions_noOutputShape() {
        Integration integration =
            createIntegrationFromDataShapes(dataShape(NONE), null);

        FilterOptions options = handler.getFilterOptions(integration, -1);
        assertThat(options.getPaths()).isEmpty();
    }

    private Integration createIntegrationFromDataShapes(DataShape... dataShapes) {
        return new Integration.Builder().steps(steps(dataShapes)).build();
    }

    private List<SimpleStep> steps(DataShape ... dataShapes) {
        List<SimpleStep> ret = new ArrayList<>();
        for (DataShape shape : dataShapes) {
            Action action = new Action.Builder().outputDataShape(shape).build();
            ret.add(new SimpleStep.Builder().action(action).build());
        }
        return ret;
    }

    private DataShape dataShape(String kind) {
        return dataShape(kind, null);
    }

    private DataShape dataShape(String kind, String type) {
        return new DataShape.Builder().kind(kind).type(type).build();
    }
}

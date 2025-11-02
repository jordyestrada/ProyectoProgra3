package cr.una.reservas_municipales.service.strategy;

import cr.una.reservas_municipales.model.Space;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SpaceFilterStrategy
 */
class SpaceFilterStrategyTest {

    @Test
    void testInterfaceExists() {
        assertTrue(SpaceFilterStrategy.class.isInterface());
    }

    @Test
    void testFilterMethodExists() throws NoSuchMethodException {
        SpaceFilterStrategy.class.getMethod("filter", List.class, Object.class);
    }

    @Test
    void testFilterMethodReturnsListOfSpaces() throws NoSuchMethodException {
        var method = SpaceFilterStrategy.class.getMethod("filter", List.class, Object.class);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void testFilterMethodParameters() throws NoSuchMethodException {
        var method = SpaceFilterStrategy.class.getMethod("filter", List.class, Object.class);
        var params = method.getParameterTypes();
        assertEquals(2, params.length);
        assertEquals(List.class, params[0]);
        assertEquals(Object.class, params[1]);
    }

    @Test
    void testCanImplementStrategy() {
        SpaceFilterStrategy strategy = (spaces, criteria) -> {
            List<Space> filtered = new ArrayList<>();
            for (Space space : spaces) {
                if (space.isActive()) {
                    filtered.add(space);
                }
            }
            return filtered;
        };
        
        assertNotNull(strategy);
    }

    @Test
    void testStrategyImplementationFiltersCorrectly() {
        SpaceFilterStrategy activeFilter = (spaces, criteria) -> 
            spaces.stream()
                .filter(Space::isActive)
                .toList();
        
        Space activeSpace = new Space();
        activeSpace.setSpaceId(UUID.randomUUID());
        activeSpace.setActive(true);
        
        Space inactiveSpace = new Space();
        inactiveSpace.setSpaceId(UUID.randomUUID());
        inactiveSpace.setActive(false);
        
        List<Space> allSpaces = List.of(activeSpace, inactiveSpace);
        List<Space> result = activeFilter.filter(allSpaces, null);
        
        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
    }

    @Test
    void testStrategyWithCriteria() {
        SpaceFilterStrategy capacityFilter = (spaces, criteria) -> {
            if (criteria instanceof Integer minCapacity) {
                return spaces.stream()
                    .filter(s -> s.getCapacity() != null && s.getCapacity() >= minCapacity)
                    .toList();
            }
            return spaces;
        };
        
        Space smallSpace = new Space();
        smallSpace.setSpaceId(UUID.randomUUID());
        smallSpace.setCapacity(10);
        
        Space largeSpace = new Space();
        largeSpace.setSpaceId(UUID.randomUUID());
        largeSpace.setCapacity(50);
        
        List<Space> allSpaces = List.of(smallSpace, largeSpace);
        List<Space> result = capacityFilter.filter(allSpaces, 30);
        
        assertEquals(1, result.size());
        assertEquals(50, result.get(0).getCapacity());
    }

    @Test
    void testEmptyListHandling() {
        SpaceFilterStrategy strategy = (spaces, criteria) -> spaces;
        
        List<Space> result = strategy.filter(new ArrayList<>(), null);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testNullCriteriaHandling() {
        SpaceFilterStrategy strategy = (spaces, criteria) -> spaces;
        
        Space space = new Space();
        space.setSpaceId(UUID.randomUUID());
        
        List<Space> spaces = List.of(space);
        List<Space> result = strategy.filter(spaces, null);
        
        assertEquals(1, result.size());
    }

    @Test
    void testMultipleStrategiesCanBeCreated() {
        SpaceFilterStrategy strategy1 = (spaces, criteria) -> spaces.stream()
            .filter(Space::isActive)
            .toList();
        
        SpaceFilterStrategy strategy2 = (spaces, criteria) -> spaces.stream()
            .filter(Space::isOutdoor)
            .toList();
        
        assertNotNull(strategy1);
        assertNotNull(strategy2);
        assertNotEquals(strategy1, strategy2);
    }
}

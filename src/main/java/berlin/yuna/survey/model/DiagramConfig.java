package berlin.yuna.survey.model;

import berlin.yuna.survey.logic.DiagramExporter;
import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Font;
import guru.nidi.graphviz.attribute.ForNode;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Shape;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static berlin.yuna.survey.model.DiagramConfig.ElementType.DEFAULT;

/**
 * The {@link DiagramConfig} is used to define the visible output of the diagram
 */
public class DiagramConfig {

    private int width = -1;
    private int height = -1;
    private boolean showBackTransition = false;
    private Rank.RankDir direction = Rank.RankDir.LEFT_TO_RIGHT;
    private final EnumMap<ElementType, Set<Attributes<? extends ForNode>>> attributesNode = new EnumMap<>(ElementType.class);
    private final DiagramExporter exporter;

    public DiagramConfig(final DiagramExporter exporter) {
        attributesNode.put(ElementType.ITEM_CHOICE, new HashSet<>(Set.of(Shape.OVAL)));
        attributesNode.put(ElementType.ITEM_DRAFT, new HashSet<>(Set.of(Color.BLUE)));
        attributesNode.put(DEFAULT, new HashSet<>(Set.of(Shape.RECTANGLE, Color.BLACK, Font.name("helvetica"))));
        attributesNode.put(ElementType.ITEM_CURRENT, new HashSet<>(Set.of(Color.ORANGE)));
        attributesNode.put(ElementType.ITEM_ANSWERED, new HashSet<>(Set.of(Color.GREEN)));
        this.exporter = exporter;
    }

    public enum ElementType {
        ITEM_DRAFT,
        ITEM_CHOICE,
        ITEM_CURRENT,
        ITEM_ANSWERED,
        DEFAULT,
    }

    /**
     * Width of the diagram
     *
     * @return current configured width
     */
    public int width() {
        return width;
    }

    /**
     * Sets the width of the diagram (-1 = automatic)
     *
     * @return current {@link DiagramConfig}
     */
    public DiagramConfig width(int width) {
        this.width = width;
        return this;
    }

    /**
     * Height of the diagram
     *
     * @return current configured height
     */
    public int height() {
        return height;
    }

    /**
     * Sets the height of the diagram (-1 = automatic)
     *
     * @return current {@link DiagramConfig}
     */
    public DiagramConfig height(int height) {
        this.height = height;
        return this;
    }

    /**
     * Direction of the diagram
     *
     * @return the direction {@link Rank.RankDir} of the diagram
     */
    public Rank.RankDir direction() {
        return direction;
    }

    /**
     * Sets the direction of the diagram (e.g. left to right)
     *
     * @return current {@link DiagramConfig}
     */
    public DiagramConfig direction(Rank.RankDir direction) {
        this.direction = direction;
        return this;
    }

    /**
     * Switch to show or hide the back transitions
     *
     * @return {@code true} if transitions will be shown on the diagram
     */
    public boolean showBackTransition() {
        return showBackTransition;
    }

    /**
     * Switch to show or hide the back transitions
     *
     * @return current {@link DiagramConfig}
     */
    public DiagramConfig showBackTransition(boolean showBackTransition) {
        this.showBackTransition = showBackTransition;
        return this;
    }

    /**
     * {@link DiagramExporter} for chain operations
     *
     * @return {@link DiagramExporter}
     */
    public DiagramExporter diagram() {
        return exporter;
    }

    /**
     * Adds a configuration/attribute to a specific {@link ElementType} of the diagram
     * Previous attribute with same key will be removed
     *
     * @param type      {@link ElementType} to apply the attribute
     * @param attribute configuration/attribute for the specified {@link ElementType}
     * @return current {@link DiagramConfig}
     */
    public DiagramConfig add(final ElementType type, final Attributes<? extends ForNode> attribute) {
        final Set<Attributes<? extends ForNode>> elementAttr = attributesNode.get(type);
        final Optional<Attributes<? extends ForNode>> previousItem = get(type, toKey(attribute));
        previousItem.ifPresent(elementAttr::remove);
        elementAttr.add(attribute);
        return this;
    }

    /**
     * Check if a key is already defined in the config
     *
     * @param type {@link ElementType} to apply the attribute
     * @param key  configuration/attribute key for the specified {@link ElementType}
     * @return {@code true} if the key exists in the configuration
     */
    public boolean containsKey(final ElementType type, final String key) {
        return get(type, key).isPresent();

    }

    /**
     * Gets a specific configuration/attribute for the given key and {@link ElementType}
     *
     * @param type         {@link ElementType} for the configuration/attribute
     * @param key          configuration/attribute key for the specified {@link ElementType}
     * @param defaultValue fallback if the key wasn't defined
     * @return configured {@link Attributes} or else value from 'defaultValue` parameter
     */
    public Attributes<? extends ForNode> getOrDefault(final ElementType type, final String key, final Attributes<? extends ForNode> defaultValue) {
        return get(type, key).orElse(defaultValue);
    }

    /**
     * Gets a specific configuration/attribute for the given key and {@link ElementType}
     *
     * @param type {@link ElementType} for the configuration/attribute
     * @param key  configuration/attribute key to search for
     * @return configured {@link Optional#empty()} if the key wasn't found in the configuration
     */
    public Optional<Attributes<? extends ForNode>> get(final ElementType type, final String key) {
        return attributesNode.get(type).stream().filter(attr -> key.equals(toKey(attr))).findFirst()
                .or(() -> type != DEFAULT ? get(DEFAULT, key) : Optional.empty());
    }

    /**
     * Gets all configurations/attributes for the given {@link ElementType}
     *
     * @param type {@link ElementType} for the configuration/attribute
     * @return a set of {@link Attributes}
     */
    public Set<Attributes<? extends ForNode>> get(final ElementType type) {
        final Set<Attributes<? extends ForNode>> result = new HashSet<>(attributesNode.get(DEFAULT));
        result.removeAll(attributesNode.get(type));
        result.addAll(attributesNode.get(type));
        return result;
    }

    /**
     * Extracts an {@link Attributes} to its key value
     *
     * @param attribute {@link Attributes} to extract key from
     * @return key from {@link Attributes}
     */
    public static String toKey(final Attributes<? extends ForNode> attribute) {
        final Iterator<Map.Entry<String, Object>> iterator = attribute.iterator();
        return iterator.hasNext() ? iterator.next().getKey() : null;
    }

}

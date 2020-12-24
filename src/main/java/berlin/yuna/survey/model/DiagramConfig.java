package berlin.yuna.survey.model;

import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.ForNode;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Shape;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static berlin.yuna.survey.model.DiagramConfig.ElementType.ITEM_DEFAULT;

public class DiagramConfig {

    private int width = -1;
    private int height = -1;
    private Rank.RankDir direction = Rank.RankDir.LEFT_TO_RIGHT;
    private final EnumMap<ElementType, Set<Attributes<? extends ForNode>>> attributes = new EnumMap<>(ElementType.class);

    public DiagramConfig() {
        attributes.put(ElementType.ITEM_CHOICE, new HashSet<>(Set.of(Shape.OVAL)));
        attributes.put(ElementType.ITEM_DRAFT, new HashSet<>(Set.of(Color.BLUE)));
        attributes.put(ITEM_DEFAULT, new HashSet<>(Set.of(Shape.RECTANGLE, Color.BLACK)));
        attributes.put(ElementType.ITEM_CURRENT, new HashSet<>(Set.of(Color.ORANGE)));
        attributes.put(ElementType.ITEM_ANSWERED, new HashSet<>(Set.of(Color.GREEN)));
    }

    public enum ElementType {
        ITEM_DRAFT,
        ITEM_CHOICE,
        ITEM_CURRENT,
        ITEM_ANSWERED,
        ITEM_DEFAULT,
    }

    public int width() {
        return width;
    }

    public DiagramConfig width(int width) {
        this.width = width;
        return this;
    }

    public int height() {
        return height;
    }

    public DiagramConfig height(int height) {
        this.height = height;
        return this;
    }

    public Rank.RankDir direction() {
        return direction;
    }

    public DiagramConfig direction(Rank.RankDir direction) {
        this.direction = direction;
        return this;
    }

    public DiagramConfig add(final ElementType type, final Attributes<? extends ForNode> attribute) {
        final Set<Attributes<? extends ForNode>> elementAttr = attributes.get(type);
        final Optional<Attributes<? extends ForNode>> previousItem = get(type, toKey(attribute));
        previousItem.ifPresent(elementAttr::remove);
        elementAttr.add(attribute);
        return this;
    }

    public boolean containsKey(final ElementType type, final String key) {
        return get(type, key).isPresent();

    }

    public Attributes<? extends ForNode> getOrDefault(final ElementType type, final String key, final Attributes<? extends ForNode> defaultValue) {
        return get(type, key).orElse(defaultValue);
    }

    public Optional<Attributes<? extends ForNode>> get(final ElementType type, final String key) {
        return attributes.get(type).stream().filter(attr -> key.equals(toKey(attr))).findFirst()
                .or(() -> type != ITEM_DEFAULT ? get(ITEM_DEFAULT, key) : Optional.empty());
    }

    public Set<Attributes<? extends ForNode>> get(final ElementType type) {
        final Set<Attributes<? extends ForNode>> result = new HashSet<>(attributes.get(ITEM_DEFAULT));
        result.removeAll(attributes.get(type));
        result.addAll(attributes.get(type));
        return result;
    }

    public static String toKey(final Attributes<? extends ForNode> attribute) {
        final Iterator<Map.Entry<String, Object>> iterator = attribute.iterator();
        return iterator.hasNext() ? iterator.next().getKey() : null;
    }

}

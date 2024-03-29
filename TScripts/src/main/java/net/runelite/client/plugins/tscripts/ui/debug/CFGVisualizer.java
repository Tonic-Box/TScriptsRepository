package net.runelite.client.plugins.tscripts.ui.debug;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.shape.mxRectangleShape;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import net.runelite.client.plugins.tscripts.util.TextUtil;
import net.runelite.client.plugins.tscripts.util.controlflow.*;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Visualizes the control flow graph of a given Script
 */
public class CFGVisualizer extends JFrame {
    private final static JsonParser jsonParser = new JsonParser();
    private final mxGraph graph;
    private final Object parent;
    private final Map<String, Object> nodesMap;
    private final Map<String,String> edgeLabels = new HashMap<>();
    private final AlphabetIterator alphabetIterator = new AlphabetIterator("");
    private final NumericIterator numericIterator = new NumericIterator();
    private final ScopeStack scopeStack;
    private int nodeCounter = 0;
    private final Map<Object,String> linkBacks = new HashMap<>();

    /**
     * Creates a new CFGVisualizer
     * @param jsonStr The JSON string representing the AST of the script
     */
    public static void create(String jsonStr) {
        CFGVisualizer frame = new CFGVisualizer(jsonStr);
        frame.setSize(800, 600);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
    }

    /**
     * Constructs a new CFGVisualizer
     * @param jsonStr The JSON string representing the AST of the script
     */
    private CFGVisualizer(String jsonStr) {
        super("Control Flow Graph");

        this.graph = new mxGraph();
        this.graph.setCellsEditable(false);
        this.graph.setAutoOrigin(true);
        this.graph.setAutoSizeCells(true);
        this.graph.setHtmlLabels(true);
        this.graph.setAllowDanglingEdges(true);
        setStyles();
        this.parent = graph.getDefaultParent();
        this.nodesMap = new HashMap<>();
        graph.getModel().beginUpdate();
        try {
            scopeStack = new ScopeStack();
            JsonObject ast = jsonParser.parse(jsonStr).getAsJsonObject();
            processNode(ast, null);
        } finally {
            graph.getModel().endUpdate();
            applyTreeLayout();
            graph.setCellsMovable(false);
            graph.setCellsResizable(false);
            graph.setCellsEditable(false);
            graph.setEdgeLabelsMovable(false);
            graph.setConnectableEdges(false);
        }

        applyTreeLayout();

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        getContentPane().add(graphComponent);
        edgeLabels.clear();
        nodesMap.clear();
    }

    /**
     * Sets the styles for the graph
     */
    private void setStyles() {
        Map<String, Object> edgeStyle = this.graph.getStylesheet().getDefaultEdgeStyle();
        edgeStyle.put(mxConstants.STYLE_ROUNDED, true);
        edgeStyle.put(mxConstants.STYLE_ELBOW, mxConstants.ELBOW_VERTICAL);
        edgeStyle.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_DIAMOND);
        edgeStyle.put(mxConstants.STYLE_TARGET_PERIMETER_SPACING, 1d);
        edgeStyle.put(mxConstants.STYLE_STROKEWIDTH, 1.25d);

        Map<String, Object> vertexStyle = this.graph.getStylesheet().getDefaultVertexStyle();
        vertexStyle.put(mxConstants.STYLE_AUTOSIZE, 1);
        vertexStyle.put(mxConstants.STYLE_SPACING, "5");
        vertexStyle.put(mxConstants.STYLE_ORTHOGONAL, "true");
        vertexStyle.put(mxConstants.STYLE_ROUNDED, true);
        vertexStyle.put(mxConstants.STYLE_ARCSIZE, 5);
        vertexStyle.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_LEFT);
        mxGraphics2DCanvas.putShape(mxConstants.SHAPE_RECTANGLE, new mxRectangleShape() {
            @Override
            protected int getArcSize(mxCellState state, double w, double h) {
                return 10;
            }
        });
        mxStylesheet stylesheet = new mxStylesheet();
        stylesheet.setDefaultEdgeStyle(edgeStyle);
        stylesheet.setDefaultVertexStyle(vertexStyle);
        this.graph.setStylesheet(stylesheet);
    }

    /**
     * Processes a node in the AST
     * @param node The node to process
     * @param parentId The ID of the parent node
     */
    private void processNode(JsonObject node, String parentId) {
        String edgeLabel = "";
        int stackNumber = numericIterator.getNextNumber();
        String label = "<html>" + colorize("//Block-" + stackNumber, Colors.NOTATION) + "\n" + createLabelFromScope(node) + "</html>";
        String nodeHash = JsonHashUtil.getSha256Hash(node);
        if(edgeLabels.containsKey(nodeHash))
        {
            edgeLabel = edgeLabels.get(nodeHash);
        }
        Object graphNode = graph.insertVertex(parent, null, label, 0, 0, 80, 30,"fillColor=" + Colors.BACKGROUND + ";fontSize=12;");
        graph.updateCellSize(graphNode); // Resize node to fit content
        nodesMap.put(label, graphNode);

        if (parentId != null) {
            graph.insertEdge(parent, null, edgeLabel, nodesMap.get(parentId), graphNode, "fontSize=12;");
        }

        if(!linkBacks.isEmpty())
        {
            linkBacks.clear();
        }

        boolean isWhileCondition = node.has("condition") &&
                "WHILE".equals(node.get("condition").getAsJsonObject().get("type").getAsString());

        scopeStack.push(stackNumber, isWhileCondition, graphNode);
        if (node.has("elements")) {
            JsonObject elements = node.getAsJsonObject("elements");
            for (String key : elements.keySet()) {
                JsonObject element = elements.getAsJsonObject(key);
                if(!element.get("type").getAsString().equals("SCOPE"))
                    continue;
                processNode(elements.getAsJsonObject(key), label);
            }
        }
        scopeStack.pop();
    }

    /**
     * Creates a label for a scope node
     * @param node The scope node
     * @return The label
     */
    private String createLabelFromScope(JsonObject node)
    {
        String counter = colorize("[" + nodeCounter++ + "]", Colors.BACKGROUND);
        StringBuilder label = new StringBuilder();
        String tab = "";
        String close = "";

        if (node.has("condition"))
        {
            JsonObject condition = node.get("condition").getAsJsonObject();
            String type = condition.get("type").getAsString().toLowerCase();
            String left;
            String right = "";
            String comparator = "";
            if (condition.has("right"))
            {
                try
                {
                    right = colorize(condition.get("right").getAsString(), Colors.VALUES);
                } catch (Exception e)
                {
                    JsonObject leftObj = condition.get("right").getAsJsonObject();
                    right = createLabelFromNode(leftObj);
                }
                String temp = condition.get("comparator").getAsString();
                switch (temp) {
                    case "GT":
                        comparator = " > ";
                        break;
                    case "LT":
                        comparator = " < ";
                        break;
                    case "GTEQ":
                        comparator = " >= ";
                        break;
                    case "LTEQ":
                        comparator = " <= ";
                        break;
                    case "EQ":
                        comparator = " == ";
                        break;
                    case "NEQ":
                        comparator = " != ";
                        break;
                    default:
                        // comparator remains unchanged
                        break;
                }
            }
            try
            {
                left = colorize(condition.get("left").getAsString(), Colors.VALUES);
            } catch (Exception e)
            {
                JsonObject leftObj = condition.get("left").getAsJsonObject();
                left = createLabelFromNode(leftObj);
            }
            label.append(colorize(type, Colors.KEYWORDS)).append(colorize("(", Colors.OPERATORS))
                    .append(left)
                    .append(colorize(comparator, Colors.OPERATORS))
                    .append(right).append(colorize(") {", Colors.OPERATORS));
            close = colorize("\n}", Colors.OPERATORS);
            tab = colorize(". . . ", Colors.BACKGROUND);
        }

        if (node.has("elements")) {
            JsonObject elements = node.getAsJsonObject("elements");
            for (String key : elements.keySet()) {
                JsonObject element = elements.getAsJsonObject(key);
                if(element.get("type").getAsString().equals("SCOPE"))
                {
                    String newScopeLabel = alphabetIterator.getNextLetter();
                    label.append("\n").append(tab).append(colorize("[scope] ", Colors.FUNCTIONS)).append(colorize("//flows to edge " + newScopeLabel, Colors.NOTATION));
                    edgeLabels.put(JsonHashUtil.getSha256Hash(element), "<html>" + colorize(newScopeLabel, Colors.EDGE_LABEL_COLOR) + "</html>");
                    continue;
                }
                label.append("\n").append(tab).append(createLabelFromNode(element));
            }
        }
        return cleanLabel(label + close + counter);
    }

    /**
     * Creates a label for a node
     * @param node The node
     * @return The label
     */
    private String createLabelFromNode(JsonObject node) {
        String nodeType = node.get("type").getAsString();
        String label = "";
        String flowTo = "";

        if (nodeType.equals("VARIABLE_ASSIGNMENT")) {
            String varName = node.get("var").getAsString();
            JsonArray values = node.getAsJsonArray("values");
            JsonElement element = values.get(0);
            String valuesStr;

            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                if (obj.has("type") && obj.get("type").getAsString().equals("FUNCTION_CALL")) {
                    valuesStr = createLabelFromNode(obj);
                } else {
                    valuesStr = colorize(values.get(0).getAsString(), Colors.VALUES);
                }
            } else {
                valuesStr = colorize(values.get(0).getAsString(), Colors.VALUES);
            }

            String type = node.get("assignmentType").getAsString();
            switch (type) {
                case "INCREMENT":
                    valuesStr = colorize(" += ", Colors.OPERATORS) + valuesStr;
                    break;
                case "DECREMENT":
                    valuesStr = colorize(" -= ", Colors.OPERATORS) + valuesStr;
                    break;
                default:
                    valuesStr = colorize(" = ", Colors.OPERATORS) + valuesStr;
                    break;
            }
            label += colorize(varName, Colors.VARIABLES) + valuesStr;
        } else if (nodeType.equals("FUNCTION_CALL")) {
            String name = colorize(node.get("name").getAsString(), Colors.FUNCTIONS);
            JsonArray values = node.getAsJsonArray("args");
            StringBuilder valuesStr = new StringBuilder();
            if (values != null) {
                for (JsonElement element : values) {
                    if (element.isJsonObject()) {
                        JsonObject obj = element.getAsJsonObject();
                        if (obj.has("type") && obj.get("type").getAsString().equals("FUNCTION_CALL")) {
                            valuesStr.append(createLabelFromNode(obj)).append(", ");
                        } else {
                            valuesStr.append(colorize(element.getAsString(), Colors.VALUES)).append(", ");
                        }
                    } else {
                        valuesStr.append(colorize(element.getAsString(), Colors.VALUES)).append(", ");
                    }
                }
                if (valuesStr.length() > 0) {
                    valuesStr.setLength(valuesStr.length() - 2); // Remove the last comma and space
                }
            }
            label += name + colorize("(", Colors.OPERATORS) + valuesStr + colorize(")", Colors.OPERATORS);

            if (name.contains(">break<")) {
                int to = scopeStack._break(linkBacks);
                flowTo = colorize(" //Flows to block-" + to, Colors.NOTATION);
            } else if (name.contains(">continue<")) {
                int to = scopeStack._continue(linkBacks);
                flowTo = colorize(" //Flows to block-" + to, Colors.NOTATION);
            }
        } else {
            label += " " + nodeType;
        }

        return label + flowTo;
    }

    /**
     * Applies a tree layout to the graph
     */
    private void applyTreeLayout() {
        mxCompactTreeLayout layout = new mxCompactTreeLayout(graph, false);
        layout.setEdgeRouting(true);
        layout.setLevelDistance(20);
        layout.setNodeDistance(10);
        layout.setMoveTree(true);
        layout.execute(parent);
    }

    /**
     * Colorizes a string
     * @param str The string to colorize
     * @param color The color to use
     * @return The colorized string
     */
    private static String colorize(String str, String color) {
        str = TextUtil.escapeHtml(str);
        return "<font color=\"" + color + "\">" + str + "</font>";
    }

    /**
     * Cleans a label
     * @param str The label to clean
     * @return The cleaned label
     */
    public static String cleanLabel(String str) {
        if (str.startsWith("\n"))
        {
            return str.substring(1);
        }
        return str.replaceAll("(?m)^[ \t]*\r?\n", "");
    }
}
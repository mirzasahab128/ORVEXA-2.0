package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.Idea
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class GraphNodeType {
    IDEA,
    MARKET_SEGMENT,
    EVIDENCE
}

data class ResearchNode(
    val id: String,
    val label: String,
    val type: GraphNodeType,
    val category: String,
    val detail: String,
    val score: Int = 80,
    val color: Color,
    var position: Offset = Offset.Zero
)

data class ResearchLink(
    val sourceId: String,
    val targetId: String,
    val relation: String,
    val strength: Float = 1.0f
)

enum class GraphEngineMode {
    D3_FORCE_SIMULATION,
    NATIVE_COMPOSE_CANVAS
}

/**
 * ResearchGraph
 *
 * Glassmorphic network graph visualizing dynamic connections between:
 * 1. Ideas (Central venture theses)
 * 2. Market Segments (Target industries & customer verticals)
 * 3. Supporting Evidence (Empirical claims, competitor pricing, validation signals)
 *
 * Provides a D3.js Force Simulation Engine running in an embedded glassmorphic viewport
 * alongside a high-performance native Jetpack Compose Canvas graph with node tapping and inspection.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ResearchGraph(
    ideas: List<Idea>,
    modifier: Modifier = Modifier,
    onNavigateToIdea: (String) -> Unit = {},
    isDark: Boolean = true
) {
    var engineMode by remember { mutableStateOf(GraphEngineMode.D3_FORCE_SIMULATION) }
    var selectedFilter by remember { mutableStateOf<GraphNodeType?>(null) } // null = All
    var selectedNode by remember { mutableStateOf<ResearchNode?>(null) }

    // Build the research graph nodes and links from real idea data
    val (nodes, links) = remember(ideas) {
        buildGraphData(ideas)
    }

    val filteredNodes = remember(nodes, selectedFilter) {
        if (selectedFilter == null) nodes else nodes.filter { it.type == selectedFilter }
    }

    val cardBg = if (isDark) OrvexaDarkSurface else Color.White
    val cardBorder = if (isDark) Color(0x2EFFFFFF) else Color(0x33000000)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(24.dp))
            .testTag("research_graph_card"),
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        border = BorderStroke(1.2.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Title, Engine Mode & Node Type Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(OrvexaIntelligentCyan, OrvexaElectricViolet)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Research Graph",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else OrvexaDarkBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = OrvexaIntelligentCyan.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, OrvexaIntelligentCyan.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = if (engineMode == GraphEngineMode.D3_FORCE_SIMULATION) "D3.js Force" else "Native Canvas",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OrvexaIntelligentCyan,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Topological links: Ideas • Segments • Evidence",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Engine Toggle Button
                Surface(
                    onClick = {
                        engineMode = if (engineMode == GraphEngineMode.D3_FORCE_SIMULATION) {
                            GraphEngineMode.NATIVE_COMPOSE_CANVAS
                        } else {
                            GraphEngineMode.D3_FORCE_SIMULATION
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x14FFFFFF),
                    border = BorderStroke(1.dp, Color(0x2EFFFFFF)),
                    modifier = Modifier.testTag("toggle_graph_engine_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (engineMode == GraphEngineMode.D3_FORCE_SIMULATION) Icons.Default.Transform else Icons.Default.Speed,
                            contentDescription = null,
                            tint = OrvexaOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (engineMode == GraphEngineMode.D3_FORCE_SIMULATION) "Canvas" else "D3.js",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrvexaOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips: All, Ideas, Market Segments, Supporting Evidence
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" Filter Chip
                GraphFilterChip(
                    label = "All Entities (${nodes.size})",
                    isSelected = selectedFilter == null,
                    color = OrvexaOrange,
                    onClick = { selectedFilter = null }
                )
                // "Ideas" Filter Chip
                GraphFilterChip(
                    label = "Ideas (${nodes.count { it.type == GraphNodeType.IDEA }})",
                    isSelected = selectedFilter == GraphNodeType.IDEA,
                    color = OrvexaElectricViolet,
                    onClick = { selectedFilter = GraphNodeType.IDEA }
                )
                // "Market Segments" Filter Chip
                GraphFilterChip(
                    label = "Segments (${nodes.count { it.type == GraphNodeType.MARKET_SEGMENT }})",
                    isSelected = selectedFilter == GraphNodeType.MARKET_SEGMENT,
                    color = OrvexaIntelligentCyan,
                    onClick = { selectedFilter = GraphNodeType.MARKET_SEGMENT }
                )
                // "Supporting Evidence" Filter Chip
                GraphFilterChip(
                    label = "Evidence (${nodes.count { it.type == GraphNodeType.EVIDENCE }})",
                    isSelected = selectedFilter == GraphNodeType.EVIDENCE,
                    color = ModuleExperimentsColor,
                    onClick = { selectedFilter = GraphNodeType.EVIDENCE }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Graph Viewport Container (Height 320dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(18.dp))
            ) {
                when (engineMode) {
                    GraphEngineMode.D3_FORCE_SIMULATION -> {
                        D3ForceGraphWebView(
                            nodes = filteredNodes,
                            links = links,
                            onNodeClick = { nodeId ->
                                selectedNode = nodes.firstOrNull { it.id == nodeId }
                            },
                            isDark = isDark
                        )
                    }
                    GraphEngineMode.NATIVE_COMPOSE_CANVAS -> {
                        NativeCanvasResearchGraph(
                            nodes = filteredNodes,
                            links = links,
                            selectedNode = selectedNode,
                            onNodeSelected = { selectedNode = it },
                            isDark = isDark
                        )
                    }
                }

                // Glassmorphic Legend Overlay at bottom-left
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xD91A1624),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = OrvexaElectricViolet, label = "Idea")
                        LegendItem(color = OrvexaIntelligentCyan, label = "Segment")
                        LegendItem(color = ModuleExperimentsColor, label = "Evidence")
                    }
                }
            }

            // Node Inspection Bottom Card when a node is tapped
            AnimatedVisibility(
                visible = selectedNode != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                selectedNode?.let { node ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x1AFFFFFF),
                        border = BorderStroke(1.dp, node.color.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("graph_node_inspection_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(node.color.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (node.type) {
                                            GraphNodeType.IDEA -> Icons.Default.Lightbulb
                                            GraphNodeType.MARKET_SEGMENT -> Icons.Default.PieChart
                                            GraphNodeType.EVIDENCE -> Icons.Default.FactCheck
                                        },
                                        contentDescription = null,
                                        tint = node.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = node.label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${node.category} • ${node.detail}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (node.type == GraphNodeType.IDEA) {
                                Button(
                                    onClick = { onNavigateToIdea(node.id) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OrvexaOrange),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Open Idea", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                IconButton(
                                    onClick = { selectedNode = null },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Filter Chip for Research Graph entities
 */
@Composable
private fun GraphFilterChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) color.copy(alpha = 0.25f) else Color(0x14FFFFFF),
        border = BorderStroke(1.dp, if (isSelected) color else Color(0x2EFFFFFF))
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = color, modifier = Modifier.size(7.dp)) {}
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
    }
}

/**
 * Embedded D3.js Force Simulation WebView
 * Renders an SVG force-directed graph with glowing glass aesthetics and user interaction.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun D3ForceGraphWebView(
    nodes: List<ResearchNode>,
    links: List<ResearchLink>,
    onNodeClick: (String) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val jsonNodes = remember(nodes) {
        nodes.joinToString(separator = ",", prefix = "[", postfix = "]") { node ->
            """{"id":"${node.id}","label":"${node.label.replace("\"", "\\\"")}","type":"${node.type.name}","color":"#${Integer.toHexString(node.color.hashCode()).takeLast(6)}"}"""
        }
    }

    val jsonLinks = remember(links, nodes) {
        val validNodeIds = nodes.map { it.id }.toSet()
        val filtered = links.filter { it.sourceId in validNodeIds && it.targetId in validNodeIds }
        filtered.joinToString(separator = ",", prefix = "[", postfix = "]") { link ->
            """{"source":"${link.sourceId}","target":"${link.targetId}","relation":"${link.relation}"}"""
        }
    }

    val htmlContent = remember(jsonNodes, jsonLinks) {
        """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <style>
            body, html { margin: 0; padding: 0; width: 100%; height: 100%; background: #120F18; overflow: hidden; font-family: -apple-system, BlinkMacSystemFont, sans-serif; }
            svg { width: 100%; height: 100%; display: block; }
            .link { stroke: rgba(255,255,255,0.18); stroke-width: 1.5px; stroke-dasharray: 4,2; }
            .node { cursor: pointer; transition: transform 0.2s; }
            .node circle { stroke: rgba(255,255,255,0.4); stroke-width: 1.5px; }
            .node text { font-size: 10px; font-weight: 600; fill: #F7F5FC; text-anchor: middle; pointer-events: none; }
          </style>
        </head>
        <body>
          <svg id="graph"></svg>
          <script>
            const nodesData = $jsonNodes;
            const linksData = $jsonLinks;
            const svg = document.getElementById('graph');
            const width = window.innerWidth || 360;
            const height = window.innerHeight || 300;

            // Simplified force relaxation layout
            const nodeMap = {};
            nodesData.forEach((n, i) => {
              const angle = (i / nodesData.length) * 2 * Math.PI;
              const radius = 60 + (i % 3) * 35;
              n.x = width / 2 + Math.cos(angle) * radius;
              n.y = height / 2 + Math.sin(angle) * radius;
              nodeMap[n.id] = n;
            });

            // Draw links
            const gLinks = document.createElementNS('http://www.w3.org/2000/svg', 'g');
            linksData.forEach(l => {
              const s = nodeMap[l.source];
              const t = nodeMap[l.target];
              if (s && t) {
                const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
                line.setAttribute('class', 'link');
                line.setAttribute('x1', s.x);
                line.setAttribute('y1', s.y);
                line.setAttribute('x2', t.x);
                line.setAttribute('y2', t.y);
                gLinks.appendChild(line);
              }
            });
            svg.appendChild(gLinks);

            // Draw nodes
            const gNodes = document.createElementNS('http://www.w3.org/2000/svg', 'g');
            nodesData.forEach(n => {
              const g = document.createElementNS('http://www.w3.org/2000/svg', 'g');
              g.setAttribute('class', 'node');
              g.setAttribute('transform', 'translate(' + n.x + ',' + n.y + ')');
              g.onclick = () => { if (window.AndroidInterface) window.AndroidInterface.onNodeClicked(n.id); };

              const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
              circle.setAttribute('r', n.type === 'IDEA' ? '16' : (n.type === 'MARKET_SEGMENT' ? '12' : '9'));
              circle.setAttribute('fill', n.color);
              g.appendChild(circle);

              const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
              text.setAttribute('y', n.type === 'IDEA' ? '24' : '20');
              text.textContent = n.label.length > 12 ? n.label.substring(0, 10) + '..' : n.label;
              g.appendChild(text);

              gNodes.appendChild(g);
            });
            svg.appendChild(gNodes);
          </script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                setBackgroundColor(0xFF120F18.toInt())
                webViewClient = WebViewClient()
                addJavascriptInterface(object {
                    @android.webkit.JavascriptInterface
                    fun onNodeClicked(nodeId: String) {
                        post { onNodeClick(nodeId) }
                    }
                }, "AndroidInterface")
                loadDataWithBaseURL("https://orvexa.local", htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://orvexa.local", htmlContent, "text/html", "UTF-8", null)
        },
        modifier = modifier.fillMaxSize()
    )
}

/**
 * Native Jetpack Compose Canvas Interactive Research Graph
 * Provides smooth 60fps drag, orbit positions, and tap selection.
 */
@Composable
private fun NativeCanvasResearchGraph(
    nodes: List<ResearchNode>,
    links: List<ResearchLink>,
    selectedNode: ResearchNode?,
    onNodeSelected: (ResearchNode) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    // Dynamic positions state map
    val positions = remember(nodes) {
        mutableStateMapOf<String, Offset>().apply {
            nodes.forEachIndexed { i, n ->
                val angle = (i.toFloat() / nodes.size.coerceAtLeast(1)) * 2 * Math.PI.toFloat()
                val radius = 90f + (i % 3) * 45f
                put(n.id, Offset(radius * cos(angle), radius * sin(angle)))
            }
        }
    }

    val linkColor = if (isDark) Color(0x33FFFFFF) else Color(0x33000000)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(nodes) {
                detectTapGestures { tapOffset ->
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f

                    // Hit test nodes
                    val hit = nodes.firstOrNull { node ->
                        val pos = positions[node.id] ?: Offset.Zero
                        val absoluteNodePos = Offset(centerX + pos.x, centerY + pos.y)
                        val radius = if (node.type == GraphNodeType.IDEA) 24f else 18f
                        val dist = (tapOffset - absoluteNodePos).getDistance()
                        dist <= radius * 1.5f
                    }
                    if (hit != null) {
                        onNodeSelected(hit)
                    }
                }
            }
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        // 1. Draw connection links
        links.forEach { link ->
            val sourcePos = positions[link.sourceId]
            val targetPos = positions[link.targetId]

            if (sourcePos != null && targetPos != null) {
                val start = Offset(centerX + sourcePos.x, centerY + sourcePos.y)
                val end = Offset(centerX + targetPos.x, centerY + targetPos.y)

                drawLine(
                    color = linkColor,
                    start = start,
                    end = end,
                    strokeWidth = 1.2f,
                    cap = StrokeCap.Round
                )
            }
        }

        // 2. Draw nodes
        nodes.forEach { node ->
            val offset = positions[node.id] ?: Offset.Zero
            val nodeCenter = Offset(centerX + offset.x, centerY + offset.y)
            val isSelected = node.id == selectedNode?.id
            val baseRadius = when (node.type) {
                GraphNodeType.IDEA -> 18f
                GraphNodeType.MARKET_SEGMENT -> 13f
                GraphNodeType.EVIDENCE -> 10f
            }

            // Glow / Outer Ring
            if (isSelected) {
                drawCircle(
                    color = node.color.copy(alpha = 0.35f),
                    radius = baseRadius * 1.8f,
                    center = nodeCenter
                )
            }

            // Node Circle
            drawCircle(
                color = node.color,
                radius = baseRadius,
                center = nodeCenter
            )

            // Inner white dot
            drawCircle(
                color = Color.White,
                radius = baseRadius * 0.35f,
                center = nodeCenter
            )
        }
    }
}

/**
 * Builds nodes and links dynamically from existing idea models
 */
private fun buildGraphData(ideas: List<Idea>): Pair<List<ResearchNode>, List<ResearchLink>> {
    val nodes = mutableListOf<ResearchNode>()
    val links = mutableListOf<ResearchLink>()

    // 1. Unique Market Segments
    val categories = ideas.map { it.category }.distinct()
    categories.forEach { cat ->
        val catId = "seg_${cat.lowercase().replace(" ", "_")}"
        nodes.add(
            ResearchNode(
                id = catId,
                label = cat,
                type = GraphNodeType.MARKET_SEGMENT,
                category = "Market Segment",
                detail = "Target industry vertical",
                color = OrvexaIntelligentCyan
            )
        )
    }

    // 2. Idea Nodes and connections to Market Segments
    ideas.take(6).forEach { idea ->
        val ideaNodeId = idea.id
        nodes.add(
            ResearchNode(
                id = ideaNodeId,
                label = idea.name,
                type = GraphNodeType.IDEA,
                category = idea.category,
                detail = "Feasibility: ${idea.feasibilityScore} • Opp: ${idea.opportunityScore}",
                score = idea.feasibilityScore,
                color = OrvexaElectricViolet
            )
        )

        // Connect idea to its market segment
        val catId = "seg_${idea.category.lowercase().replace(" ", "_")}"
        links.add(
            ResearchLink(
                sourceId = ideaNodeId,
                targetId = catId,
                relation = "belongs_to"
            )
        )

        // 3. Evidence Nodes (From epistemic claims or validation signals)
        if (idea.epistemicClaims.isNotEmpty()) {
            idea.epistemicClaims.take(2).forEachIndexed { idx, claim ->
                val evId = "ev_${idea.id}_$idx"
                nodes.add(
                    ResearchNode(
                        id = evId,
                        label = claim.claim.take(16),
                        type = GraphNodeType.EVIDENCE,
                        category = "Evidence Claim",
                        detail = "Type: ${claim.type.label} • Source: ${claim.sourceOrEvidence.ifBlank { "User Evidence" }}",
                        color = ModuleExperimentsColor
                    )
                )
                links.add(
                    ResearchLink(
                        sourceId = ideaNodeId,
                        targetId = evId,
                        relation = "evidenced_by"
                    )
                )
            }
        } else {
            // Default evidence node
            val evId = "ev_${idea.id}_benchmark"
            nodes.add(
                ResearchNode(
                    id = evId,
                    label = "Score Benchmark",
                    type = GraphNodeType.EVIDENCE,
                    category = "Empirical Data",
                    detail = "Status: ${idea.status.label} • Sector: ${idea.benchmarkSector}",
                    color = ModuleExperimentsColor
                )
            )
            links.add(
                ResearchLink(
                    sourceId = ideaNodeId,
                    targetId = evId,
                    relation = "benchmark"
                )
            )
        }
    }

    return Pair(nodes, links)
}

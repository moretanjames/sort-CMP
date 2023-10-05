import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun App() {
  MaterialTheme {
    var detailsPerson by remember { mutableStateOf<TreePerson?>(null) }
    Surface(color = Color.LightGray) {
      var zoom by rememberSaveable { mutableStateOf(1f) }
      var panX by rememberSaveable { mutableStateOf(0f) }
      var panY by rememberSaveable { mutableStateOf(0f) }
      val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        zoom *= zoomChange
        panX += panChange.x
        panY += panChange.y
      }

      Box(modifier = Modifier.fillMaxSize().transformable(transformState), contentAlignment = Alignment.Center) {
        Tree(
          modifier = Modifier
            .graphicsLayer {
              scaleX = zoom
              scaleY = zoom
              translationX = panX
              translationY = panY
            }
        ) {
          treeGraph.forEach {
            TreeNode(
              person = it,
              onClick = { detailsPerson = it },
              modifier = Modifier.ahnentafel(it.ahnentafelNum)
            )
          }
        }
      }
    }

    AnimatedVisibility(
      visible = detailsPerson != null,
      enter = slideInVertically { it },
      exit = slideOutVertically { it }
    ) {
      Scaffold(
        topBar = {
          TopAppBar(
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 4.dp,
            title = {},
            navigationIcon = {
              IconButton(onClick = { detailsPerson = null }) {
                Icon(Icons.Default.ArrowBack, null)
              }
            }
          )
        }
      ) {
        Box(modifier = Modifier.padding(it).fillMaxSize(), contentAlignment = Alignment.Center) {
          Text("Details for ${detailsPerson?.name}")
        }
      }
    }
  }
}

@Composable
fun Tree(
  modifier: Modifier = Modifier,
  verticalSpacing: Dp = 8.dp,
  horizontalSpacing: Dp = 8.dp,
  content: @Composable TreeScope.() -> Unit
) {
  val treeScopedContent = @Composable { TreeScope().content() }

  Layout(content = treeScopedContent, modifier = modifier) { children, constraints ->

    val placeables = children.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }

    val nodeHeight = placeables.first().height
    val nodeWidth = placeables.first().width
    val treeVerticalLevels = placeables.maxBy { it.ahnentafelNum }.ahnentafelString.length
    val treeHorizontalLevels = treeVerticalLevels.pow(2) / 2

    val maxHeight = (nodeHeight * treeVerticalLevels) + (verticalSpacing.roundToPx() * (treeVerticalLevels - 1))
    val maxWidth = (nodeWidth * treeHorizontalLevels) + (horizontalSpacing.roundToPx() * (treeHorizontalLevels + 1))
    layout(maxWidth, maxHeight) {
      placeables.forEach { treeNode ->
        val ahnentafel = treeNode.ahnentafelString
        val y = maxHeight - ((ahnentafel.length * nodeHeight) + ((ahnentafel.length - 1) * verticalSpacing.roundToPx()))
        val widthPerNode = ahnentafel.length.takeIf { it > 1 }?.let { maxWidth / (it.pow(2) / 2) } ?: maxWidth
        var x = 0
        ahnentafel.takeIf { it.length > 1 }?.substring(1)?.forEachIndexed { index, c ->
          x += (if (c == '1') 1 else 0) * (maxWidth / 2.pow(index + 1))
        }
        x += (widthPerNode - nodeWidth) / 2

        treeNode.place(x = x, y = y)
      }
    }
  }
}

@LayoutScopeMarker
class TreeScope {
  fun Modifier.ahnentafel(ahnentafelNum: Int) = then(TreeChildData(ahnentafelNum))
}

data class TreeChildData(val ahnentafelNum: Int) : ParentDataModifier {
  override fun Density.modifyParentData(parentData: Any?): Any = this@TreeChildData

}

val Placeable.ahnentafelNum get() = (parentData as TreeChildData).ahnentafelNum
val Placeable.ahnentafelString get() = ahnentafelNum.toString(2)

data class TreePerson(
  val name: String,
  val ahnentafelNum: Int,
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TreeNode(
  person: TreePerson,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(shape = MaterialTheme.shapes.medium, modifier = modifier, onClick = onClick) {
    Column(
      modifier = Modifier.padding(4.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(Icons.Outlined.AccountCircle, null)
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = person.name, style = MaterialTheme.typography.caption.copy(fontSize = 8.sp), modifier = Modifier.width(40.dp), maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
  }
}


val treeGraph = listOf(
  TreePerson(
    name = "Tanner Harding",
    ahnentafelNum = 0b1
  ),
  TreePerson(
    name = "Robert Harding",
    ahnentafelNum = 0b10
  ),
  TreePerson(
    name = "Kathy Cottle",
    ahnentafelNum = 0b11
  ),
  TreePerson(
    name = "George Harding",
    ahnentafelNum = 0b100
  ),
  TreePerson(
    name = "Kathlene Smith",
    ahnentafelNum = 0b101
  ),
  TreePerson(
    name = "Keneth Cottle",
    ahnentafelNum = 0b110
  ),
  TreePerson(
    name = "Donna Butler",
    ahnentafelNum = 0b111
  ),
  TreePerson(
    name = "Ralph Harding",
    ahnentafelNum = 0b1000
  ),
  TreePerson(
    name = "Kathryn Olsen",
    ahnentafelNum = 0b1001
  ),
  TreePerson(
    name = "Karl Smith",
    ahnentafelNum = 0b1010
  ),
  TreePerson(
    name = "Retha Dukes",
    ahnentafelNum = 0b1011
  ),
  TreePerson(
    name = "Kenneth Cottle",
    ahnentafelNum = 0b1100
  ),
  TreePerson(
    name = "Nina Leavitt",
    ahnentafelNum = 0b1101
  ),
  TreePerson(
    name = "Allan Butler",
    ahnentafelNum = 0b1110
  ),
  TreePerson(
    name = "Freda Hiatt",
    ahnentafelNum = 0b1111
  ),
)


fun Int.pow(exp: Int): Int {
  return List(exp) { this }.fold(1) { acc, next -> acc * next }
}
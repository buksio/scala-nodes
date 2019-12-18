package pl.com.britenet.nodes

import java.io.File

import net.liftweb.json.{DefaultFormats, Serialization}
import org.apache.poi.ss.usermodel.{CellType, Row, WorkbookFactory}

import scala.jdk.CollectionConverters._

case class Node(id: Int, name: String, nodes: List[Node])

case class RawNode(id: Int, name: String, row: Int, col: Int)

object Main {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty || args.length < 2) {
      throw new RuntimeException("Must provide path (String) and depth (Int)")
    }

    val workbook = WorkbookFactory.create(new File(args(0)))
    val orphanage = new Orphanage

    val listOfRows = workbook.getSheetAt(0).iterator.asScala.toList
    val families = orphanage.createFamilies(listOfRows, args(1).toInt)

    implicit val formats: DefaultFormats.type = DefaultFormats
    println(Serialization.writePretty(families))
  }
}

class Orphanage {

  def createFamilies(list: List[Row], depth: Int): List[Node] = {
    val all = this.mapRawNodes(list, depth)
    val parents = all.groupBy(node => node.col)(0)
    createFamily(parents, all)
  }

  private def mapRawNodes(rows: List[Row], depth: Int): List[RawNode] = {
    val isRowANode = (row: Row) => row.getCell(depth).getCellType == CellType.NUMERIC
    rows.filter(isRowANode).zipWithIndex.map {
      case (row, index) => mapRow(row, index, depth)
    }
  }

  private def createFamily(parents: List[RawNode], orphanedChildren: List[RawNode]): List[Node] = {
    parents match {
      case List() => List[Node]()
      case List(x) =>
        List[Node](Node(x.id, x.name, createFamily(assignChildren(x, orphanedChildren), orphanedChildren)))
      case x :: xs =>
        Node(x.id, x.name, createFamily(assignChildren(x, orphanedChildren), orphanedChildren)) :: createFamily(xs, orphanedChildren)
    }
  }

  private def assignChildren(parent: RawNode, orphanedChildren: List[RawNode]): List[RawNode] = {
    val nextParent = orphanedChildren.find(x => x.col == parent.col && x.row > parent.row)

    if (nextParent.isEmpty)
      orphanedChildren.filter(child => isNextGen(parent, child) && isAfter(parent, child))
    else
      orphanedChildren.filter(child => isNextGen(parent, child) && isAfter(parent, child) && isBefore(nextParent.get, child))
  }

  private def isNextGen(parent: RawNode, child: RawNode): Boolean = {
    parent.col + 1 == child.col
  }

  private def isBefore(parent: RawNode, child: RawNode): Boolean = {
    parent.row > child.row
  }

  private def isAfter(parent: RawNode, child: RawNode): Boolean = {
    parent.row < child.row
  }

  private def mapRow(row: Row, index: Int, depth: Int): RawNode = {
    val id = row.getCell(depth).getNumericCellValue.intValue

    val cellAndIndex = row.cellIterator().asScala.toList.zipWithIndex.find {
      case (cell, _) => cell.getCellType == CellType.STRING
    }.get

    val name = cellAndIndex._1.getStringCellValue
    val col = cellAndIndex._2

    RawNode(id, name, index, col)
  }
}

package sparsity.statement

import sparsity.{Expression, Column, Name}

sealed abstract class ColumnAnnotation

case class ColumnIsPrimaryKey() extends ColumnAnnotation
case class ColumnIsNotNullable() extends ColumnAnnotation
case class ColumnDefaultValue(v: Expression) extends ColumnAnnotation

case class ColumnDefinition(name: Name, t: Name, annotations: Seq[ColumnAnnotation] = Seq())

package sparsity

import org.specs2.mutable.Specification
import sparsity.parser.{Expression => Parse}
import sparsity.expression._

class ExpressionParserSpec extends Specification 
{
  "The Expression Parser" should {

    "Parse Comparisons" >> {
      Parse("1 > 2") should be equalTo(
        Comparison(LongPrimitive(1), Comparison.Gt, LongPrimitive(2))
      )
    }

    "Parse Arithmetic" >> {
      Parse("1+2") should be equalTo(
        Arithmetic(LongPrimitive(1), Arithmetic.Add, LongPrimitive(2))
      )
      Parse("1 + 2") should be equalTo(
        Arithmetic(LongPrimitive(1), Arithmetic.Add, LongPrimitive(2))
      )
    }

    "Respect Order of Operations" >> {
      Parse("1+2*3") should be equalTo(
        Arithmetic(LongPrimitive(1), Arithmetic.Add, 
          Arithmetic(LongPrimitive(2), Arithmetic.Mult, LongPrimitive(3))
        )
      )
      Parse("1+2 > 3") should be equalTo(
        Comparison(
          Arithmetic(LongPrimitive(1), Arithmetic.Add, LongPrimitive(2)),
          Comparison.Gt, LongPrimitive(3)
        )
      )
      Parse("1+2 > 3 AND true") should be equalTo(
        Arithmetic(
          Comparison(
            Arithmetic(LongPrimitive(1), Arithmetic.Add, LongPrimitive(2)),
            Comparison.Gt, LongPrimitive(3)
          ),
          Arithmetic.And,
          BooleanPrimitive(true)
        )
      )
    }

    "Parse NULL tests" >> {
      Parse("1 IS NULL") should be equalTo(
        IsNull(LongPrimitive(1))
      )
      Parse("1 IS NOT NULL") should be equalTo(
        Not(IsNull(LongPrimitive(1)))
      )
    }

    "Parse NULL literals" >> {
      Parse("NULL") should be equalTo(
        NullPrimitive()
      )
    }

    "Parse Variables" >> {
      Parse("A") should be equalTo(
        Column(Name("A"))
      )
      Parse("R.A") should be equalTo(
        Column(Name("A"), Some(Name("R")))
      )
      Parse("`A`") should be equalTo(
        Column(Name("A", true))
      )
      Parse("`R`.A") should be equalTo(
        Column(Name("A"), Some(Name("R", true)))
      )
      Parse("Foo.`Bar`") should be equalTo(
        Column(Name("Bar", true), Some(Name("Foo")))
      )
      Parse("int") should be equalTo(
        Column(Name("int"))
      )
      Parse("_c0") should be equalTo(
        Column(Name("_c0"))
      )
    }

    "Parse Strings" >> {
      Parse("'Foo'") should be equalTo(StringPrimitive("Foo"))
      Parse("'Foo''sball'") should be equalTo(StringPrimitive("Foo'sball"))
      Parse("'(SEX = ''M'' ) OR ( SEX = ''F'')'") should be equalTo(StringPrimitive("(SEX = 'M' ) OR ( SEX = 'F')"))
    }

    "Parse IN Expressions" >> {
      Parse("R IN ('1', '2', '3')") should be equalTo(
        InExpression(
          Column(Name("R")), 
          Left(Seq(
            StringPrimitive("1"),
            StringPrimitive("2"),
            StringPrimitive("3")
          ))
        )
      )
      Parse("R.`_c1` IN ('1', '2', '3')") should be equalTo(
        InExpression(
          Column(Name("_c1", true), Some(Name("R"))), 
          Left(Seq(
            StringPrimitive("1"),
            StringPrimitive("2"),
            StringPrimitive("3")
          ))
        )
      )
    }

    "Parse CASE Expressions" >> {
      Parse("CASE WHEN A > 1 THEN B ELSE C END") should be equalTo(
        CaseWhenElse(
          None,
          Seq( Comparison(
                  Column(Name("A")), 
                  Comparison.Gt, 
                  LongPrimitive(1)
                ) -> Column(Name("B"))
          ),
          Column(Name("C"))
        )
      )
      Parse("CASE A WHEN 1 THEN B WHEN 2 THEN C ELSE D END") should be equalTo(
        CaseWhenElse(
          Some(Column(Name("A"))),
          Seq( 
            LongPrimitive(1) -> Column(Name("B")),
            LongPrimitive(2) -> Column(Name("C"))
          ),
          Column(Name("D"))
        )
      )
      Parse("IF A = 1 THEN B ELSE C END") should be equalTo(
        CaseWhenElse(
          None,
          Seq(Comparison(
            Column(Name("A")),
            Comparison.Eq,
            LongPrimitive(1)
          ) -> Column(Name("B"))),
          Column(Name("C"))
        )
      )
    }

    "Parse BETWEEN Expressions" >> {
      Parse("A BETWEEN 1 AND 2") should be equalTo(
        Arithmetic(
          Comparison(
            Column(Name("A")),
            Comparison.Gte,
            LongPrimitive(1)
          ),
          Arithmetic.And,
          Comparison(
            Column(Name("A")),
            Comparison.Lte,
            LongPrimitive(2)
          )
        )
      )
      Parse("A+2 BETWEEN 1 AND 2") should be equalTo(
        Arithmetic(
          Comparison(
            Arithmetic(
              Column(Name("A")),
              Arithmetic.Add,
              LongPrimitive(2)
            ),
            Comparison.Gte,
            LongPrimitive(1)
          ),
          Arithmetic.And,
          Comparison(
            Arithmetic(
              Column(Name("A")),
              Arithmetic.Add,
              LongPrimitive(2)
            ),
            Comparison.Lte,
            LongPrimitive(2)
          )
        )
      )
    }

  }
}

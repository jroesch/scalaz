package scalaz

////
/**
 * [[scalaz.Applicative]] without `point`.
 */
////
trait Apply[F[_]] extends Functor[F] { self =>
  ////
  def ap[A,B](fa: => F[A])(f: => F[A => B]): F[B]

  // derived functions

  def traverse1[A, G[_], B](value: G[A])(f: A => F[B])(implicit G: Traverse1[G]): F[G[B]] =
    G.traverse1(value)(f)(this)

  def sequence1[A, G[_]: Traverse1](as: G[F[A]]): F[G[A]] =
    traverse1(as)(a => a)

  /**The composition of Applys `F` and `G`, `[x]F[G[x]]`, is a Apply */
  def compose[G[_]](implicit G0: Apply[G]): Apply[({type λ[α] = F[G[α]]})#λ] = new CompositionApply[F, G] {
    implicit def F = self

    implicit def G = G0
  }

  /**The product of Applys `F` and `G`, `[x](F[x], G[x]])`, is a Apply */
  def product[G[_]](implicit G0: Apply[G]): Apply[({type λ[α] = (F[α], G[α])})#λ] = new ProductApply[F, G] {
    implicit def F = self

    implicit def G = G0
  }

  /** Flipped variant of `ap`. */
  def apF[A,B](f: => F[A => B]): F[A] => F[B] = ap(_)(f)

  /** [[scalaz.Zip]] derived from `tuple2`. */
  def zip: Zip[F] =
    new Zip[F] {
      def zip[A, B](a: => F[A], b: => F[B]): F[(A, B)] =
        apply2(a, b)((x, y) => (x, y))
    }

  def ap2[A,B,C](fa: => F[A], fb: => F[B])(f: F[(A,B) => C]): F[C] =
    ap(fb)(ap(fa)(map(f)(_.curried)))
  def ap3[A,B,C,D](fa: => F[A], fb: => F[B], fc: => F[C])(f: F[(A,B,C) => D]): F[D] =
    ap(fc)(ap2(fa,fb)(map(f)(f => ((a:A,b:B) => (c:C) => f(a,b,c)))))
  def ap4[A,B,C,D,E](fa: => F[A], fb: => F[B], fc: => F[C], fd: => F[D])(f: F[(A,B,C,D) => E]): F[E] =
    ap2(fc, fd)(ap2(fa,fb)(map(f)(f => ((a:A,b:B) => (c:C, d:D) => f(a,b,c,d)))))
  def ap5[A,B,C,D,E,R](fa: => F[A], fb: => F[B], fc: => F[C], fd: => F[D], fe: => F[E])(f: F[(A,B,C,D,E) => R]): F[R] =
    ap2(fd, fe)(ap3(fa,fb,fc)(map(f)(f => ((a:A,b:B,c:C) => (d:D, e:E) => f(a,b,c,d,e)))))
  def ap6[A,B,C,D,E,FF, R](fa: => F[A], fb: => F[B], fc: => F[C], fd: => F[D], fe: => F[E], ff: => F[FF])(f: F[(A,B,C,D,E,FF) => R]): F[R] =
    ap3(fd, fe, ff)(ap3(fa,fb,fc)(map(f)(f => ((a:A,b:B,c:C) => (d:D, e:E, ff: FF) => f(a,b,c,d,e,ff)))))
  def ap7[A,B,C,D,E,FF,G,R](fa: => F[A], fb: => F[B], fc: => F[C], fd: => F[D], fe: => F[E], ff: => F[FF], fg: => F[G])(f: F[(A,B,C,D,E,FF,G) => R]): F[R] =
    ap3(fe, ff, fg)(ap4(fa,fb,fc,fd)(map(f)(f => ((a:A,b:B,c:C,d: D) => (e:E, ff: FF, g: G) => f(a,b,c,d,e,ff,g)))))
  def ap8[A,B,C,D,E,FF,G,H,R](fa: => F[A], fb: => F[B], fc: => F[C], fd: => F[D], fe: => F[E], ff: => F[FF], fg: => F[G], fh: => F[H])(f: F[(A,B,C,D,E,FF,G,H) => R]): F[R] =
    ap4(fe, ff, fg, fh)(ap4(fa,fb,fc,fd)(map(f)(f => ((a:A,b:B,c:C,d: D) => (e:E, ff: FF, g: G, h: H) => f(a,b,c,d,e,ff,g,h)))))

  @deprecated("given `F: Apply[F]` use `F(a,b)(f)` instead, or given `implicitly[Apply[F]]`, use `^(a,b)(f)`", "7")
  def map2[A, B, C](fa: => F[A], fb: => F[B])(f: (A, B) => C): F[C] =
    apply2(fa,fb)(f)

  @deprecated("given `F: Apply[F]` use `F(a,b,c)(f)` instead, or given `implicitly[Apply[F]]`, use `^(a,b,c)(f)`", "7")
  def map3[A, B, C, D](fa: => F[A], fb: => F[B], fc: => F[C])(f: (A, B, C) => D): F[D] =
    apply3(fa,fb,fc)(f)

  @deprecated("given `F: Apply[F]` use `F(a,b,c,d)(f)` instead, or given `implicitly[Apply[F]]`, use `^(a,b,c,d)(f)`", "7")
  def map4[A, B, C, D, E](fa: => F[A], fb: => F[B], fc: => F[C], fd: => F[D])(f: (A, B, C, D) => E): F[E] =
    apply4(fa,fb,fc,fd)(f)

  def apply2[A, B, C](fa: => F[A], fb: => F[B])(f: (A, B) => C): F[C] = ap(fb)(map(fa)(f.curried))

  def apply3[A, B, C, D](fa: => F[A], fb: => F[B], fc: => F[C])(f: (A, B, C) => D): F[D] =
    apply2(apply2(fa, fb)((_, _)), fc)((ab, c) => f(ab._1, ab._2, c))
  def apply4[A, B, C, D, E](fa: => F[A], fb: => F[B], fc: => F[C], fd: => F[D])(f: (A, B, C, D) => E): F[E] =
    apply2(apply2(fa, fb)((_, _)), apply2(fc, fd)((_, _)))((t, d) => f(t._1, t._2, d._1, d._2))
  def apply5[A, B, C, D, E, R](fa: => F[A], fb: => F[B], fc: => F[C], fd: => F[D], fe: => F[E])(f: (A, B, C, D, E) => R): F[R] =
    apply2(apply3(fa, fb, fc)((_, _, _)), apply2(fd, fe)((_, _)))((t, t2) => f(t._1, t._2, t._3, t2._1, t2._2))
  def apply6[A, B, C, D, E, FF, R](fa: => F[A], fb: => F[B], fc: => F[C], fd: => F[D], fe: => F[E], ff: => F[FF])(f: (A, B, C, D, E, FF) => R): F[R] =
    apply2(apply3(fa, fb, fc)((_, _, _)), apply3(fd, fe, ff)((_, _, _)))((t, t2) => f(t._1, t._2, t._3, t2._1, t2._2, t2._3))
  def apply7[A, B, C, D, E, FF, G, R](fa: => F[A], fb: => F[B], fc: => F[C], fd: => F[D], fe: => F[E], ff: => F[FF], fg: => F[G])(f: (A, B, C, D, E, FF, G) => R): F[R] =
    apply2(apply4(fa, fb, fc, fd)((_, _, _, _)), apply3(fe, ff, fg)((_, _, _)))((t, t2) => f(t._1, t._2, t._3, t._4, t2._1, t2._2, t2._3))
  def apply8[A, B, C, D, E, FF, G, H, R](fa: => F[A], fb: => F[B], fc: => F[C], fd: => F[D], fe: => F[E], ff: => F[FF], fg: => F[G], fh: => F[H])(f: (A, B, C, D, E, FF, G, H) => R): F[R] =
    apply2(apply4(fa, fb, fc, fd)((_, _, _, _)), apply4(fe, ff, fg, fh)((_, _, _, _)))((t, t2) => f(t._1, t._2, t._3, t._4, t2._1, t2._2, t2._3, t2._4))
  def apply9[A, B, C, D, E, FF, G, H, I, R](fa: => F[A], fb: => F[B], fc: => F[C], fd: => F[D],
                                          fe: => F[E], ff: => F[FF], fg: => F[G], fh: => F[H], fi: => F[I])(f: (A, B, C, D, E, FF, G, H, I) => R): F[R] =
    apply3(apply3(fa, fb, fc)((_, _, _)), apply3(fd, fe, ff)((_, _, _)), apply3(fg, fh, fi)((_, _, _)))((t, t2, t3) => f(t._1, t._2, t._3, t2._1, t2._2, t2._3, t3._1, t3._2, t3._3))
  def apply10[A, B, C, D, E, FF, G, H, I, J, R](fa: => F[A], fb: => F[B], fc: => F[C], fd: => F[D],
                                          fe: => F[E], ff: => F[FF], fg: => F[G], fh: => F[H],
                                          fi: => F[I], fj: => F[J])(f: (A, B, C, D, E, FF, G, H, I, J) => R): F[R] =
    apply3(apply3(fa, fb, fc)((_, _, _)), apply3(fd, fe, ff)((_, _, _)), apply4(fg, fh, fi, fj)((_, _, _, _)))((t, t2, t3) => f(t._1, t._2, t._3, t2._1, t2._2, t2._3, t3._1, t3._2, t3._3, t3._4))
  def apply11[A, B, C, D, E, FF, G, H, I, J, K, R](fa: => F[A], fb: => F[B], fc: => F[C], fd: => F[D],
                                          fe: => F[E], ff: => F[FF], fg: => F[G], fh: => F[H],
                                          fi: => F[I], fj: => F[J], fk: => F[K])(f: (A, B, C, D, E, FF, G, H, I, J, K) => R): F[R] =
    apply3(apply3(fa, fb, fc)((_, _, _)), apply4(fd, fe, ff, fg)((_, _, _, _)), apply4(fh, fi, fj, fk)((_, _, _, _)))((t, t2, t3) => f(t._1, t._2, t._3, t2._1, t2._2, t2._3, t2._4, t3._1, t3._2, t3._3, t3._4))
  def apply12[A, B, C, D, E, FF, G, H, I, J, K, L, R](fa: => F[A], fb: => F[B], fc: => F[C], fd: => F[D],
                                          fe: => F[E], ff: => F[FF], fg: => F[G], fh: => F[H],
                                          fi: => F[I], fj: => F[J], fk: => F[K], fl: => F[L])(f: (A, B, C, D, E, FF, G, H, I, J, K, L) => R): F[R] =
    apply3(apply4(fa, fb, fc, fd)((_, _, _, _)), apply4(fe, ff, fg, fh)((_, _, _, _)), apply4(fi, fj, fk, fl)((_, _, _, _)))((t, t2, t3) => f(t._1, t._2, t._3, t._4, t2._1, t2._2, t2._3, t2._4, t3._1, t3._2, t3._3, t3._4))

  def tuple2[A,B](fa: => F[A], fb: => F[B]): F[(A,B)] =
    apply2(fa, fb)((_,_))
  def tuple3[A,B,C](fa: => F[A], fb: => F[B], fc: F[C]): F[(A,B,C)] =
    apply3(fa, fb, fc)((_,_,_))
  def tuple4[A,B,C,D](fa: => F[A], fb: => F[B], fc: => F[C], fd: => F[D]): F[(A,B,C,D)] =
    apply4(fa, fb, fc, fd)((_,_,_,_))
  def tuple5[A,B,C,D,E](fa: => F[A], fb: => F[B], fc: => F[C], fd: => F[D], fe: => F[E]): F[(A,B,C,D,E)] =
    apply5(fa, fb, fc, fd, fe)((_,_,_,_,_))

  def lift2[A, B, C](f: (A, B) => C): (F[A], F[B]) => F[C] =
    apply2(_, _)(f)
  def lift3[A, B, C, D](f: (A, B, C) => D): (F[A], F[B], F[C]) => F[D] =
    apply3(_, _, _)(f)
  def lift4[A, B, C, D, E](f: (A, B, C, D) => E): (F[A], F[B], F[C], F[D]) => F[E] =
    apply4(_, _, _, _)(f)
  def lift5[A, B, C, D, E, R](f: (A, B, C, D, E) => R): (F[A], F[B], F[C], F[D], F[E]) => F[R] =
    apply5(_, _, _, _, _)(f)
  def lift6[A, B, C, D, E, FF, R](f: (A, B, C, D, E, FF) => R): (F[A], F[B], F[C], F[D], F[E], F[FF]) => F[R] =
    apply6(_, _, _, _, _, _)(f)
  def lift7[A, B, C, D, E, FF, G, R](f: (A, B, C, D, E, FF, G) => R): (F[A], F[B], F[C], F[D], F[E], F[FF], F[G]) => F[R] =
    apply7(_, _, _, _, _, _, _)(f)
  def lift8[A, B, C, D, E, FF, G, H, R](f: (A, B, C, D, E, FF, G, H) => R): (F[A], F[B], F[C], F[D], F[E], F[FF], F[G], F[H]) => F[R] =
    apply8(_, _, _, _, _, _, _, _)(f)
  def lift9[A, B, C, D, E, FF, G, H, I, R](f: (A, B, C, D, E, FF, G, H, I) => R): (F[A], F[B], F[C], F[D], F[E], F[FF], F[G], F[H], F[I]) => F[R] =
    apply9(_, _, _, _, _, _, _, _, _)(f)
  def lift10[A, B, C, D, E, FF, G, H, I, J, R](f: (A, B, C, D, E, FF, G, H, I, J) => R): (F[A], F[B], F[C], F[D], F[E], F[FF], F[G], F[H], F[I], F[J]) => F[R] =
    apply10(_, _, _, _, _, _, _, _, _, _)(f)
  def lift11[A, B, C, D, E, FF, G, H, I, J, K, R](f: (A, B, C, D, E, FF, G, H, I, J, K) => R): (F[A], F[B], F[C], F[D], F[E], F[FF], F[G], F[H], F[I], F[J], F[K]) => F[R] =
    apply11(_, _, _, _, _, _, _, _, _, _, _)(f)
  def lift12[A, B, C, D, E, FF, G, H, I, J, K, L, R](f: (A, B, C, D, E, FF, G, H, I, J, K, L) => R): (F[A], F[B], F[C], F[D], F[E], F[FF], F[G], F[H], F[I], F[J], F[K], F[L]) => F[R] =
    apply12(_, _, _, _, _, _, _, _, _, _, _, _)(f)

  ////
  val applySyntax = new scalaz.syntax.ApplySyntax[F] { def F = Apply.this }
}

object Apply {
  @inline def apply[F[_]](implicit F: Apply[F]): Apply[F] = F

  ////

  ////
}

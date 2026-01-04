package app.simple.inure.internals

/**
 * Marker span used by [app.simple.inure.extensions.fragments.FinderScopedFragment] to track matches.
 *
 * Kept as a distinct type so we can remove finder highlights without
 * touching other formatting/background spans.
 */
internal class FinderMatchSpan
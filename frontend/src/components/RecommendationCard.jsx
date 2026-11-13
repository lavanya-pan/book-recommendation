// One recommended book with the reason it was recommended.
export default function RecommendationCard({ book }) {
  return (
    <article className="flex flex-col rounded-xl border border-[var(--color-accent)] p-4 shadow-sm">
      <h3 className="text-lg font-semibold leading-snug">{book.title}</h3>
      <p className="mt-1 text-sm opacity-80">{book.author}</p>
      {book.genre && (
        <span className="mt-3 w-fit rounded-full bg-[var(--color-accent)] px-3 py-1 text-xs font-medium text-[var(--color-bg)]">
          {book.genre}
        </span>
      )}
      <p className="mt-3 text-sm italic opacity-80">{book.reason}</p>
    </article>
  );
}

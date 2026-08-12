export default function ErrorBanner({ message, className = '' }) {
  if (!message) return null;
  return (
    <div role="alert" className={`flex items-center gap-2 rounded-lg bg-red-500/10 border border-red-500/20 px-4 py-2.5 text-sm text-red-400 animate-scale-in ${className}`}>
      <span aria-hidden="true">⚠</span><span>{message}</span>
    </div>
  );
}

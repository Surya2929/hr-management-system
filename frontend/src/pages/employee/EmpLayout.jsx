import { Outlet } from 'react-router-dom';
import TopNav from '../../components/TopNav';

export default function EmpLayout() {
  return (
    <div className="min-h-screen bg-neutral-200 overflow-x-hidden">
      <TopNav />
      <main className="w-full min-w-0 px-4 sm:px-5 md:px-6 lg:px-8 pt-4 md:pt-2 pb-10">
        <div className="w-full max-w-[1600px] mx-auto">
          <Outlet />
        </div>
      </main>
    </div>
  );
}

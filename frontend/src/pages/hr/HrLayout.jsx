import { Outlet } from 'react-router-dom';
import Sidebar from '../../components/Sidebar';

export default function HrLayout() {
  return (
    <div className="min-h-screen bg-slate-100 md:flex overflow-x-hidden">
      <Sidebar />
      <main className="w-full min-w-0 flex-1 pt-20 md:pt-0 p-4 sm:p-5 md:p-6 lg:p-8 overflow-x-hidden">
        <div className="w-full max-w-[1600px] mx-auto">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
import PublicLayout from "@/components/layout/PublicLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { HardHat, UserCircle2, Users } from "lucide-react";
import { Link } from "wouter";

export default function RoleSelect() {
  return (
    <PublicLayout>
      <div className="container flex items-center justify-center min-h-[calc(100vh-4rem-20rem)] py-20">
        <Card className="w-full max-w-2xl shadow-lg">
          <CardHeader className="text-center space-y-2">
            <CardTitle className="text-2xl font-bold">접속할 역할을 선택해주세요</CardTitle>
            <CardDescription>
              회원님은 현재 3개의 역할 권한을 보유하고 있습니다.
            </CardDescription>
          </CardHeader>
          <CardContent className="grid md:grid-cols-3 gap-4 p-6">
            {/* Site Manager Role */}
            <Link href="/site/dashboard">
              <Button variant="outline" className="w-full h-auto flex-col p-6 hover:border-secondary hover:bg-secondary/5 transition-all group space-y-4">
                <div className="h-16 w-16 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 group-hover:scale-110 transition-transform">
                  <HardHat className="h-8 w-8" />
                </div>
                <div className="text-center space-y-1">
                  <span className="font-bold text-lg block">현장 관리자</span>
                  <span className="text-xs text-muted-foreground block">Site Manager</span>
                </div>
                <div className="text-xs bg-muted px-2 py-1 rounded">
                  접속 가능 현장: 2개
                </div>
              </Button>
            </Link>

            {/* Team Leader Role */}
            <Link href="/worker/dashboard">
              <Button variant="outline" className="w-full h-auto flex-col p-6 hover:border-orange-500 hover:bg-orange-50 transition-all group space-y-4">
                <div className="h-16 w-16 rounded-full bg-orange-100 flex items-center justify-center text-orange-600 group-hover:scale-110 transition-transform">
                  <Users className="h-8 w-8" />
                </div>
                <div className="text-center space-y-1">
                  <span className="font-bold text-lg block">노무 팀장</span>
                  <span className="text-xs text-muted-foreground block">Team Leader</span>
                </div>
                <div className="text-xs bg-muted px-2 py-1 rounded">
                  소속 팀원: 12명
                </div>
              </Button>
            </Link>

            {/* Worker Role */}
            <Link href="/worker/dashboard">
              <Button variant="outline" className="w-full h-auto flex-col p-6 hover:border-green-500 hover:bg-green-50 transition-all group space-y-4">
                <div className="h-16 w-16 rounded-full bg-green-100 flex items-center justify-center text-green-600 group-hover:scale-110 transition-transform">
                  <UserCircle2 className="h-8 w-8" />
                </div>
                <div className="text-center space-y-1">
                  <span className="font-bold text-lg block">일반 노무자</span>
                  <span className="text-xs text-muted-foreground block">Worker</span>
                </div>
                <div className="text-xs bg-muted px-2 py-1 rounded">
                  최근 출역: 어제
                </div>
              </Button>
            </Link>
          </CardContent>
        </Card>
      </div>
    </PublicLayout>
  );
}


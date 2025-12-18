import { cn } from "@/lib/utils";
import { Loader2 } from "lucide-react";

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: "default" | "destructive" | "outline" | "secondary" | "ghost" | "link" | "success" | "warning";
    size?: "default" | "sm" | "lg" | "icon";
    isLoading?: boolean;
}

export function Button({ className, variant = "default", size = "default", isLoading, children, ...props }: ButtonProps) {
    const baseStyles = "inline-flex items-center justify-center whitespace-nowrap rounded-md text-base font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50";

    const variants = {
        default: "bg-[#71AA44] text-white hover:bg-[#557C2C]", // Company Green (Primary)
        secondary: "bg-gray-200 text-gray-900 hover:bg-gray-300", // Gray (Secondary)
        destructive: "bg-[#E63946] text-white hover:bg-red-600", // Red
        outline: "border border-gray-300 bg-white hover:bg-gray-50 text-gray-900",
        ghost: "hover:bg-gray-100 text-gray-900",
        link: "text-[#71AA44] underline-offset-4 hover:underline",
        success: "bg-[#71AA44] text-white hover:bg-[#557C2C]", // Company Green (Success)
        warning: "bg-[#FF8C42] text-white hover:bg-amber-600", // Orange
    };

    const sizes = {
        default: "h-12 px-6 py-3 text-base", // h-12 (48px) - 모바일 터치 영역 확보
        sm: "h-10 rounded-md px-4 text-sm", // Small
        lg: "h-12 rounded-md px-8 text-base", // Large
        icon: "h-12 w-12", // 아이콘 버튼도 h-12
    };

    return (
        <button
            className={cn(baseStyles, variants[variant], sizes[size], className)}
            disabled={isLoading || props.disabled}
            {...props}
        >
            {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            {children}
        </button>
    );
}

export function Badge({ className, variant = "default", ...props }: React.HTMLAttributes<HTMLDivElement> & { variant?: "default" | "secondary" | "outline" | "destructive" | "success" | "warning" }) {
    const variants = {
        default: "border-transparent bg-[#71AA44] text-white hover:bg-[#557C2C]",
        secondary: "border-transparent bg-[#333333] text-white hover:bg-gray-900",
        destructive: "border-transparent bg-[#E63946] text-white hover:bg-red-600",
        outline: "text-gray-900 border-gray-300",
        success: "border-transparent bg-[#C2E199] text-[#557C2C] hover:bg-[#B0D488]", // Company Green with light background
        warning: "border-transparent bg-amber-100 text-[#FF8C42] hover:bg-amber-200", // Orange with light background
    };

    return (
        <div className={cn("inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-[#71AA44] focus:ring-offset-2", variants[variant], className)} {...props} />
    );
}

export function Card({ className, children, ...props }: React.HTMLAttributes<HTMLDivElement>) {
    return (
        <div className={cn("rounded-lg border border-gray-200 bg-white text-gray-900 shadow-sm", className)} {...props}>
            {children}
        </div>
    );
}

export function CardHeader({ className, children, ...props }: React.HTMLAttributes<HTMLDivElement>) {
    return <div className={cn("flex flex-col space-y-1.5 p-5", className)} {...props}>{children}</div>; // p-5 (20px)로 변경
}

export function CardTitle({ className, children, ...props }: React.HTMLAttributes<HTMLHeadingElement>) {
    return <h3 className={cn("text-lg font-medium leading-tight tracking-tight", className)} {...props}>{children}</h3>; // font-medium, font-display 제거
}

export function CardContent({ className, children, ...props }: React.HTMLAttributes<HTMLDivElement>) {
    return <div className={cn("p-5 pt-0", className)} {...props}>{children}</div>; // p-5로 변경
}

export function Input({ className, ...props }: React.InputHTMLAttributes<HTMLInputElement>) {
    return (
        <input
            className={cn(
                "flex h-12 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-base ring-offset-background file:border-0 file:bg-transparent file:text-base file:font-medium placeholder:text-gray-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#71AA44] focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 focus:border-[#71AA44]",
                className
            )}
            {...props}
        />
    );
}
